package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.form.build.{FieldGroup, FormElement, HasElement}
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

  def remove(id: ObjectId): Unit = {
    get(id).foreach{obj =>
      obj.remove_children()
    }
    objects.remove(id)
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

  def create_default(prototype: FB.FormElement, parent_rel: Option[ParentRelation]): RuntimeObject = prototype match {
    case p: FB.GroupArray => instantiation.ArrayBuilder.default(p, parent_rel, this)
    case p: FB.FieldGroup => instantiation.GroupBuilder.default(p, parent_rel, this)
    case p: FB.AtomicField => instantiation.AtomicBuilder.default(p, parent_rel, this)
    case p: FB.GroupUnion => instantiation.GroupBuilder.default(p, parent_rel, this)
    case p: FB.Reference => instantiation.ReferenceBuilder.default(p, parent_rel, this)
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

          case Some(_: Reference) => {
            update[Reference](id, {(x: Reference) =>
              x.copy(ref_id = value.asInstanceOf[FieldValue[ObjectId]])
            })
          }
          case Some(_) => ???
          case None => ???
        }
      }
      case action: GroupArrayAction => {
        def array_getter(array_id: ObjectId): Array = get(array_id) match {
          case Some(x: Array) => x
          case _ => throw new IllegalArgumentException(s"No Array with id $array_id")
        }
        def type_getter(array: Array, symbol: Option[String]): Option[FB.FieldGroup] = {
          (array.prototype.prototype, symbol) match {
            case (union: FB.GroupUnion, Some(s)) => union.subtypes.find(g => g.symbol == s)
            case _ => None
          }

        }
        action match {
          case InsertElementBefore(array_id, id, typename) => {
            val array = array_getter(array_id)
            val concrete_type = type_getter(array, typename)
            array.insert_element(id, concrete_type, before = true)
          }
          case InsertElementAfter(array_id, id, typename) =>  {
            val array = array_getter(array_id)
            val concrete_type = type_getter(array, typename)
            array.insert_element(id, concrete_type, before = false)
          }
          case AppendElement(array_id, typename) => {
            val array = array_getter(array_id)
            val concrete_type = type_getter(array, typename)
            array.append_element(concrete_type)
          }
          case RemoveElement(array_id, id) => {
            array_getter(array_id).remove_element(id)
          }
        }
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

  def get_ref_choices(id: ObjectId): Future[Seq[LabeledValue[ObjectId]]] = {
    get(id) match {
      case Some(Reference(prototype, id, parent_rel, ref_id)) => {
        val result = search(Seq(prototype.prototype), None).map { obj =>
          val label = prototype.label_expr.flatMap(expr => eval(expr, obj) match {
            case Right(SomeValue(LabeledValue(v, _))) => Some(v.toString)
            case _ => None
          })
          LabeledValue(obj.id, label)
        }
        Future(result)
      }
      case _ => Future(Seq())
    }
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

  def search(types: Seq[FB.FormElement], flt: Option[E.PredicateExpression]): Seq[RuntimeObject] = {
    val type_set = types.toSet
    objects.values.filter {obj =>
        if (type_set.contains(obj.prototype)) {
          flt match {
            case Some(expr) => eval(expr, obj) match {
              case Right(true) => true
              case _ => false
            }
            case None => true
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
      rt.create_object(id => {
        val initial_value = prototype.default match {
          case Some(x) => SomeValue(LabeledValue(x))
          case None => NoValue
        }
        AtomicValue(prototype, initial_value, id, parent_rel)
      })
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
          case None => rt.create_default(rel.child_field, Some(ParentRelation(group, rel)))
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
    def default(prototype: FB.GroupUnion, parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      val group_prototype = prototype.subtypes.head
      val builder = new GroupBuilder(group_prototype, Seq())
      builder.build(Some(prototype), parent_rel, rt)
    }
  }

  object AbstractGroupBuilder {
    def default(prototype: FB.AbstractGroup, concrete: Option[FB.FieldGroup], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      (prototype, concrete) match {
        case (p: FB.FieldGroup, _) => GroupBuilder.default(p, parent_rel, rt)
        case (p: FB.GroupUnion, Some(c)) => {
          if (p.supertype_of(c)) {
            GroupBuilder.default(c, parent_rel, rt)
          } else {
            throw new IllegalArgumentException(s"Union ${p.symbol} is not supertype of ${c.symbol}")
          }
        }
        case (p: FB.GroupUnion, None) => GroupBuilder.default(p, parent_rel, rt)
      }
    }
  }

  case class ReferenceBuilder(q: Option[E.PredicateExpression]) extends InstanceBuilder {
    override def build(prototype_bound: Option[FB.FormElement], parent_rel: Option[ParentRelation], rt: FormRuntime): RuntimeObject = {
      prototype_bound match {
        case Some(prototype: FB.Reference) =>  {
          q match {
            case Some(query) => {
              val found = rt.search(Seq(prototype.prototype), Some(query))
              if (prototype.multiple) {
                val ref_value = MultiValue(found.map(x => LabeledValue(x.id)))
                rt.create_object(
                  id => Reference(prototype, id, parent_rel, ref_value)
                )
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
              rt.create_object(
                id => Reference(prototype, id, parent_rel, NoValue)
              )
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