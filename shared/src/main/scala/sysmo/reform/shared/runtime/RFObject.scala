package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.RFRuntime.{PropertyTypes, TreeTypes}
import sysmo.reform.shared.sources.property
import sysmo.reform.shared.sources.property.{Property, PropertySource}
import sysmo.reform.shared.sources.tree.{Dispatcher, TreeBranch, TreeLeaf, TreeNode, TreeSource}
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
  override def own_children: MIter =
    MonadicIterator.empty[F, RFObject[F]](mt)
}

case class RecordFieldInstance(dtype: TPE.RecordFieldType, instance: ObjectId)

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

  class TreeView[F[+_]](rtobj: RFObject[F]) extends TreeSource[TreeTypes, F] {
    val runtime: RFRuntime[F] = rtobj.runtime
    val mt: MonadThrow[F] = runtime.mt
    var _selection: Set[NodeId] = Set[NodeId]()


    private var _dispatcher: Dispatcher[TreeTypes, F] = new Dispatcher[TreeTypes, F] {
      override def dispatch[U <: ActionType](action: U): F[Unit] = {
        println(s"Dispatch $action")
        runtime.dispatch(action)
        renderer.foreach(_.rerender())
        runtime.mt.pure()
      }

      override def select(id: NodeId): Unit = {
        _selection = Set(id)
        renderer.foreach(_.rerender())
      }
    }

    override def selection: Set[NodeId] = _selection
    override val dispatcher: Dispatcher[TreeTypes, F] = _dispatcher
    override def root: TreeNode[TreeTypes, F] = as_node(rtobj, None)
    override def node_is_selected(id: NodeId): Boolean = selection.contains(id)

    def as_node(obj: RFObject[F], name: Option[String]): TreeNode[TreeTypes, F] = {
      obj match {
        case v: AtomicObject[F] => new DebugNode(obj)
        case rec: RecordObject[F] => new RecordObjectAsNode(rec, name.getOrElse(rec.dtype.symbol))
        case array: ArrayObject[F] => new ArrayObjectAsNode(array, name.getOrElse(""))
        //          new ArrayAsNode(array, name.getOrElse(array.prototype.symbol) + s"(${array.count})")
        //        case ref: Reference => EmptyNode
      }
    }

    def as_nodeF(id: ObjectId, name: Option[String]): F[TreeNode[TreeTypes, F]] = {
      mt.map(runtime.get(id))(obj => as_node(obj, name))
    }

    object EmptyNode extends TreeLeaf[TreeTypes, F] {
      override def parent: F[Option[TreeNode[TreeTypes, F]]] = mt.pure(None)
      override def id: Id = ObjectId.NoId
      override def name: String = "Empty"
      override def icon: Option[String] = None
      override def actions: Seq[Action] = Seq()
      override def dispatcher: Dispatcher[TreeTypes, F] = _dispatcher
      override def is_selected: Boolean = false
    }


    trait ObjectAsNode[U <: RFObject[F]] extends TreeBranch[TreeTypes, F] {
      val obj: U
      override def id: Id = obj.id
      override def dispatcher: Dispatcher[TreeTypes, F] = _dispatcher
      override def is_selected: Boolean = node_is_selected(id)
      override def parent: F[Option[TreeNode[TreeTypes, F]]] = {
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

      override def children: MonadicIterator[F, TreeNode[TreeTypes, F]] =
        MonadicIterator.empty(runtime.mt)
      override def name: String = s"${get_value.get[String]}"
      override def icon: Option[String] = Some("fa fa-bug")
      override def actions: Seq[Action] = Seq()
    }

    class RecordObjectAsNode(val obj: RecordObject[F], val name: String) extends ObjectAsNode[RecordObject[F]] {
      override def icon: Option[String] = Some("fa fa-map")
      override def actions: Seq[Action] = Seq()
      override def children: MonadicIterator[F, TreeNode[TreeTypes, F]] =
        obj.fields
//          .filterNot(x => x.target.isInstanceOf[AtomicValue])
          .flat_map(x => {
            import io.circe.syntax._
            import Encoders._
            println(x.asJson)
            as_nodeF(x.instance, Some(x.dtype.make_descr))
          })
//          .filterNot(x => x == EmptyNode)
//          .toSeq
    }

    class ArrayObjectAsNode(val obj: ArrayObject[F], val name: String) extends ObjectAsNode[ArrayObject[F]] {
      override def icon: Option[String] = Some("fa fa-list")
      override def actions: Seq[Action] = Seq()
      override def children: MonadicIterator[F, TreeNode[TreeTypes, F]] =
        obj.elements
          .flat_map(x =>
            as_nodeF(x.instance, Some(x.index.toString)))
    }
  }

  class NamedPropertyView[F[+_]] {
    class RecordObjectAsNode(val obj: RecordObject[F]) extends PropertySource[PropertyTypes, F] {
      override def props: MonadicIterator[F, Property] = ???
      override implicit val mt: MonadThrow[F] = obj.runtime.mt
      override def dispatcher: property.Dispatcher[PropertyTypes, F] = ???
    }
  }

}