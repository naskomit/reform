package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.form.build.{FormElement, HasElement}
import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.util.LabeledValue

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/** The main runtime class */
class FormRuntime(val type_graph: TP.Graph)(implicit val ec: ExecutionContext) {
  var current_id: ObjectId = ObjectId.start
  val objects: mutable.Map[ObjectId, RuntimeObject] = mutable.HashMap()
  type DoRender = () => Unit
  var force_render: Option[DoRender] = None

  def get(id: ObjectId): Option[RuntimeObject] = objects.get(id)
  def update[T <: RuntimeObject](id: ObjectId, f: T => T)(implicit ctag: ClassTag[T]): Try[T] = {
    get(id) match {
      case Some(obj: T) => {
        val new_obj = f(obj)
        new_obj.runtime = this
        objects.put(id, new_obj)
        Success(new_obj)
      }
      case Some(obj) => Failure(new IllegalStateException(s"Incorrect runtime type ${obj.getClass.getName}, should be ${ctag.getClass.getName}"))
      case None => Failure(new IllegalStateException((s"Object #$id not found")))
    }
  }

  protected def new_id: ObjectId = {
    current_id = current_id.next
    current_id
  }

  def create_object[T <: RuntimeObject](create_fn: ObjectId => T): T = {
    val obj = create_fn(new_id)
    obj.runtime = this
    objects.put(obj.id, obj)
    obj
  }

  def dispatch(action: FormAction): Unit = {
    println(action)
    action match {
      case update_action: UpdateValue => update_action match {
        case SetFieldValue(id, value) => get(id) match {
          case Some(_: AtomicValue) => {
            update[AtomicValue](id, {(x: AtomicValue) =>
              x.copy(value = value)
            })
          }
          case Some(_) => ???
          case None => ???
        }
      }
      case action: GroupArrayAction => action match {
        case InsertElementBefore(array, id) => ???
        case InsertElementAfter(array, id) => ???
        case AppendElement(array) => ???
        case RemoveArrayElement(array, id) => ???
      }
    }
    force_render.foreach(f => f())
  }

  def get_choices(id: ObjectId): Future[Seq[LabeledValue[_]]] = Future(Seq())

  def label_ref(ref: Reference): Reference = {
    def labeler(target: LabeledValue[ObjectId]): LabeledValue[ObjectId] = {
      if (target.label.isDefined) {
        target
      } else {
        (get(target.value), ref.prototype.label_expr) match {
          case (Some(value), Some(expr)) => eval(expr, value) match {
            case Right(SomeValue(LabeledValue(label, _))) => target.copy(label = Some(label.toString))
            case _ => target
          }
          case _ => target
        }
      }
    }
    ref.ref_id match {
      case NoValue => ref
      case AllValues => ref
      case SomeValue(v) => ref.copy(ref_id = SomeValue(labeler(v)))
      case MultiValue(v) => ref.copy(ref_id = MultiValue(v.map(labeler)))
    }
  }

  def get_ref_choices(id: ObjectId): Future[Seq[LabeledValue[_]]] = {
    ???
    Future(Seq())
  }

  def bind(f: DoRender): Unit = {
    force_render = Some(f)
  }
  def unbind(): Unit = {
    force_render = None
  }

  def eval(e: E.Expression, obj: RuntimeObject): E.Result[Any] = {
    E.Expression.eval(e, obj.as_context)
  }

  def search(types: Seq[FB.FormElement], q: E.PredicateExpression): Seq[RuntimeObject] = {
    val type_set = types.toSet
    objects.values.filter {obj =>
        if (type_set.contains(obj.prototype)) {
          eval(q, obj) match {
            case Right(true) => true
            case _ => false
          }
        } else
          false
      }.toSeq
    }
}

object FormRuntime {
  def apply(type_graph: TP.Graph)(implicit ec: ExecutionContext): FormRuntime = new FormRuntime(type_graph)
}

object instantiation {
  trait InstanceBuilder {
    def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject
  }

  case class AtomicBuilder(v: Any) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(p: FB.AtomicField) => {
          rt.create_object(id => AtomicValue(p, SomeValue(LabeledValue(v)), id, parent_rel))
        }
        case _ => throw new IllegalArgumentException("Prototype for AtomicValue should be of type AtomicField")
      }
    }
  }

  object AtomicBuilder {
    def default(prototype: FB.AtomicField, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject =
      rt.create_object(id => AtomicValue(prototype, NoValue, id, parent_rel))
  }

  case class ArrayBuilder(children: Seq[InstanceBuilder]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(p: FB.GroupArray) =>
          val array = rt.create_object(id => Array(p, id, parent_rel))
          children.foreach{bld =>
            val prototype_rel = p.prototype_rel
            val child_instance = bld.build(Some(prototype_rel.prototype), Some(ParentRelation(array, prototype_rel)), rt)
            array.children.append(child_instance.id)
          }
          array

        case _ => throw new IllegalArgumentException("Prototype for Array should be of type GroupArray")
      }
    }
  }

  object ArrayBuilder {
    def default(prototype: FB.GroupArray, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject =
      rt.create_object(id => Array(prototype, id, parent_rel))
  }

  case class GroupBuilder(prototype: FB.FieldGroup, children: Seq[(String, InstanceBuilder)]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(pb: FB.FieldGroup) => {
          if (prototype != pb) {
            throw new IllegalArgumentException(s"Group prototype ${prototype.symbol} does not match prototype bound ${pb.symbol}")
          }
        }
        case Some(pb: FB.GroupUnion) => {
          if (!pb.supertype_of(prototype)) {
            throw new IllegalArgumentException(s"Type ${prototype.symbol} is not subtype of ${pb.symbol}")
          }
        }
        case Some(pb) => throw new IllegalArgumentException(s"Expected object with bound ${pb}, but got FieldGroup(${prototype.symbol})")
        case None =>
      }
      val child_value_map = children.toMap
      val group = rt.create_object(id => Group(prototype, id, parent_rel))
      val field_relations: Seq[HasElement] = prototype.field_relations
      val illegal_keys = child_value_map.keys.toSet.diff(field_relations.map(_.name).toSet)
      if (illegal_keys.nonEmpty) {
        throw new IllegalArgumentException(s"No such fields $illegal_keys in group ${prototype.symbol}")
      }
      field_relations.foreach {rel =>
        val child_instance: RuntimeObject = (child_value_map.get(rel.name) match {
          case Some(v) => v.build(Some(rel.child_field), Some(ParentRelation(group, rel)), rt)
          case None => create_default(rel.child_field, Some(ParentRelation(group, rel)), rt)
        })
        group.children.addOne(rel.name -> child_instance.id)
      }
      group
    }
  }

  object GroupBuilder {
    def default(prototype: FB.FieldGroup, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      val builder = new GroupBuilder(prototype, Seq())
      builder.build(Some(prototype), parent_rel, rt)
    }

    def default(prototype: FB.GroupUnion, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = ???
  }

  case class ReferenceBuilder(q: Option[E.PredicateExpression]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(prototype: FB.Reference) =>  {
          q match {
            case Some(query) => {
              val found = rt.search(Seq(prototype.prototype), query)
              if (prototype.multiple) {
                ???
              } else {
                if (found.size == 1) {
                  val ref_value = SomeValue(LabeledValue(found.head.id))
                  rt.create_object(
                    id => Reference(prototype, id, parent_rel, ref_value)
                  )
                } else if (found.isEmpty) {
                  ReferenceBuilder.default(prototype, parent_rel, rt)
                } else {
                  throw new IllegalStateException(s"Multiple items match the reference query $query")
                }
              }
            }
            case None => if (prototype.multiple) {
              ???
            } else {
              rt.create_object(
                id => Reference(prototype, id, parent_rel, NoValue)
              )
            }
          }
        }

        case _ => throw new IllegalArgumentException("Reference can only be assigned to field with reference prototype")
      }
    }
  }

  object ReferenceBuilder {
    def default(prototype: FB.Reference, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      val builder = new ReferenceBuilder(None)
      builder.build(Some(prototype), parent_rel, rt)
    }

  }

  def create_default(prototype: FB.FormElement, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = prototype match {
    case p: FB.GroupArray => ArrayBuilder.default(p, parent_rel, rt)
    case p: FB.FieldGroup => GroupBuilder.default(p, parent_rel, rt)
    case p: FB.AtomicField => AtomicBuilder.default(p, parent_rel, rt)
    case p: FB.GroupUnion => GroupBuilder.default(p, parent_rel, rt)
    case p: FB.Reference => ReferenceBuilder.default(p, parent_rel, rt)
  }

  // Atomic values converters
  implicit def str2ab(x: String): AtomicBuilder = AtomicBuilder(x)

  implicit def seq2builder(x: Seq[InstanceBuilder]): ArrayBuilder = ArrayBuilder(x)

  def ref(q: E.PredicateExpression): ReferenceBuilder = {
    new ReferenceBuilder(Some(q))
  }

  implicit class FormGroupApply(g: FB.FieldGroup) {
    def apply(children: (String, InstanceBuilder)*): GroupBuilder = GroupBuilder(g, children)
  }


  implicit class FormRuntimeInst(rt: FormRuntime) {
    def instantiate(x: InstanceBuilder): RuntimeObject = x.build(None, None, rt)
  }
}