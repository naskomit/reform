package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.sources.SourceAction
import sysmo.reform.shared.types.{AtomicDataType, DataType}
import sysmo.reform.shared.util.{CirceTransport, MonadicIterator}
import sysmo.reform.shared.{types => TPE}


sealed trait RFObject[_F[+_]] {
  type F[+X] = _F[X]
  type MIter = MonadicIterator[F, RFObject[F]]
  val mt: MonadThrow[_F]
  type DType <: TPE.DataType
  def dtype: DType
  val id: ObjectId
  protected[runtime] var runtime: RFRuntime[F] = null
  def parent: Option[ObjectId]
  def own_children: MIter
  def get_runtime: RFRuntime[F] = runtime
}

case class ObjectProxy(id: ObjectId, dtype: TPE.DataType, parent: Option[ObjectId], value: Value)

trait AtomicObject[_F[+_]] extends RFObject[_F] {
  override type DType = TPE.AtomicDataType
  def value: Value
  def update_value(v: Value): F[Unit]
  override def own_children: MIter =
    MonadicIterator.empty[F, RFObject[F]](mt)
}

case class RecordFieldInstance(ftype: TPE.RecordFieldType, instance: ObjectId)

trait RecordObject[_F[+_]] extends RFObject[_F] {
  override type DType = TPE.RecordType
  def fields: MonadicIterator[F, RecordFieldInstance]

  private[runtime] def set_field(name: String, instance: ObjectId): F[Unit]
}

case class ArrayElementInstance[_F[+_]](index: Int, instance: ObjectId)

trait ArrayObject[_F[+_]] extends RFObject[_F] {
  override type DType = TPE.ArrayType
  def elements: MonadicIterator[F, ArrayElementInstance[F]]

  private[runtime] def add_element(instance: ObjectId): F[Unit]
}


object RFObject {
  object Encoders extends CirceTransport {
    import io.circe.syntax._
    import io.circe.generic.semiauto.deriveEncoder
    import sysmo.reform.shared.data.Transport._
    import sysmo.reform.shared.types.DataType.Encoders._

    implicit val enc_RecordFieldInstance: Encoder[RecordFieldInstance] = deriveEncoder
  }

  import sysmo.reform.shared.{sources => S}
  import S.Dispatcher
  import S.tree.{TreeSource, TreeBranch, TreeLeaf, TreeNode}
  class TreeView[F[+_]](rtobj: RFObject[F]) extends TreeSource[F] {
    val runtime: RFRuntime[F] = rtobj.runtime
    val mt: MonadThrow[F] = runtime.mt
    var _selection: Set[ObjectId] = Set[ObjectId]()


    private var _dispatcher: Dispatcher[F] = new Dispatcher[F] {

      override implicit val mt: MonadThrow[F] = runtime.mt

      override def dispatch(action: RuntimeAction): F[Unit] = {
        println(s"Dispatch $action")
        runtime.dispatch(action)
        renderer.foreach(_.rerender())
        mt.pure()
      }

      override def select(id: ObjectId): F[Unit] = {
        _selection = Set(id)
        renderer.foreach(_.rerender())
        mt.pure()
      }
    }

    override def selection: Set[ObjectId] = _selection
    override val dispatcher: Dispatcher[F] = _dispatcher
    override def root: TreeNode[F] = as_node(rtobj, None)
    override def node_is_selected(id: ObjectId): Boolean = selection.contains(id)

    def as_node(obj: RFObject[F], name: Option[String]): TreeNode[F] = {
      obj match {
        case v: AtomicObject[F] => new DebugNode(obj)
        case rec: RecordObject[F] => new RecordObjectAsNode(rec, name.getOrElse(rec.dtype.symbol))
        case array: ArrayObject[F] => new ArrayObjectAsNode(array, name.getOrElse(""))
        //          new ArrayAsNode(array, name.getOrElse(array.prototype.symbol) + s"(${array.count})")
        //        case ref: Reference => EmptyNode
      }
    }

    def as_nodeF(id: ObjectId, name: Option[String]): F[TreeNode[F]] = {
      mt.map(runtime.get(id))(obj => as_node(obj, name))
    }

    object EmptyNode extends TreeLeaf[F] {
      override def parent: F[Option[TreeNode[F]]] = mt.pure(None)
      override def id: ObjectId = ObjectId.NoId
      override def name: String = "Empty"
      override def icon: Option[String] = None
      override def actions: Seq[Action] = Seq()
      override def dispatcher: Dispatcher[F] = _dispatcher
      override def is_selected: Boolean = false
    }


    trait ObjectAsNode[U <: RFObject[F]] extends TreeBranch[F] {
      val obj: U
      override def id: ObjectId = obj.id
      override def dispatcher: Dispatcher[F] = _dispatcher
      override def is_selected: Boolean = node_is_selected(id)
      override def parent: F[Option[TreeNode[F]]] = {
        obj.parent match {
          case Some(p) => mt.map(runtime.get(p))(x => Some(as_node(x, None)))
          case None => mt.pure(None)
        }
      }
    }

    class DebugNode(val obj: RFObject[F]) extends ObjectAsNode[RFObject[F]] {
      import Value.implicits._
      private def get_value: Value = obj match {
        case atomicObject: AtomicObject[_] => atomicObject.value
        case recordObject: RecordObject[_] => Value.empty
        case arrayObject: ArrayObject[_] => Value.empty
      }

      override def children: MonadicIterator[F, TreeNode[F]] =
        MonadicIterator.empty(runtime.mt)
      override def name: String = s"${get_value.get[String]}"
      override def icon: Option[String] = Some("fa fa-bug")
      override def actions: Seq[Action] = Seq()
    }

    class RecordObjectAsNode(val obj: RecordObject[F], val name: String) extends ObjectAsNode[RecordObject[F]] {
      override def icon: Option[String] = Some("fa fa-map")
      override def actions: Seq[Action] = Seq()
      override def children: MonadicIterator[F, TreeNode[F]] =
        obj.fields
//          .filterNot(x => x.target.isInstanceOf[AtomicValue])
          .flat_map(x => {
            import io.circe.syntax._
            import Encoders._
            as_nodeF(x.instance, Some(x.ftype.make_descr))
          })
//          .filterNot(x => x == EmptyNode)
//          .toSeq
    }

    class ArrayObjectAsNode(val obj: ArrayObject[F], val name: String) extends ObjectAsNode[ArrayObject[F]] {
      override def icon: Option[String] = Some("fa fa-list")
      override def actions: Seq[Action] = Seq()
      override def children: MonadicIterator[F, TreeNode[F]] =
        obj.elements
          .flat_map(x =>
            as_nodeF(x.instance, Some(x.index.toString)))
    }
  }

  object NamedPropertyView {
    import sysmo.reform.shared.sources.property.{Property, PropertySource}
    import Value.implicits._
    class RecordObjectAsPropSource[F[+_]](val obj: RecordObject[F]) extends PropertySource[F] {
      private var _dispatcher = new Dispatcher[F] {
        implicit val mt: MonadThrow[F] = obj.runtime.mt
        override def dispatch(action: RuntimeAction): F[Unit] =
          obj.runtime.dispatch(action)
        override def select(id: ObjectId): F[Unit] = mt.pure()
      }
      override def props: MonadicIterator[F, Property] =
        obj.fields
          .flat_map {field =>
            val value_f: F[Value] = field.ftype.dtype match {
              case _: AtomicDataType =>
                mt.map(obj.runtime.get(field.instance))(inst =>
                  inst.asInstanceOf[AtomicObject[F]].value
                )
              case _ => mt.pure(Value(field.instance))
            }

            mt.map(value_f)(v => new Property {
              override def id: ObjectId = field.instance
              override def name: String = field.ftype.name
              override def descr: String = field.ftype.make_descr
              override def dtype: DataType = field.ftype.dtype
              override def value: Value = v
            })
          }

      override implicit val mt: MonadThrow[F] = obj.runtime.mt
      override def dispatcher: Dispatcher[F] = _dispatcher
    }

    class OtherAsPropSource[F[+_]](val obj: RFObject[F]) extends PropertySource[F] {
      override implicit val mt: MonadThrow[F] = obj.runtime.mt
      override def props: MonadicIterator[F, Property] = MonadicIterator.empty
      override def dispatcher: Dispatcher[F] = new Dispatcher[F] {
        override implicit val mt: MonadThrow[F] = obj.runtime.mt
        override def select(id: ObjectId): F[Unit] = mt.pure()
        override def dispatch(action: RuntimeAction): F[Unit] = ???
      }
    }

    def apply[F[+_]](obj: RFObject[F]): PropertySource[F] = obj match {
      case atomicObject: AtomicObject[F] => new OtherAsPropSource(atomicObject)
      case recordObject: RecordObject[F] => new RecordObjectAsPropSource(recordObject)
      case arrayObject: ArrayObject[F] => new OtherAsPropSource(arrayObject)
    }
  }

}