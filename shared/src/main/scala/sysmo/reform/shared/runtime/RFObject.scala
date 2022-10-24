package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.sources.SourceAction
import sysmo.reform.shared.sources.property.FieldProperty
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.table.{LocalTable, Query, Table}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, MultiReferenceType, PrimitiveDataType, RecordType, ReferenceType, UnionType}
import sysmo.reform.shared.util.{CirceTransport, MonadicIterator}
import sysmo.reform.shared.{types => TPE}


sealed trait RFObject[_F[+_]] {
  type F[+X] = _F[X]
  type MIter = MonadicIterator[F, ObjectId]
  implicit val mt: MonadThrow[_F]
  type DType <: TPE.DataType
  def dtype: DType
  val id: ObjectId
  protected[runtime] var runtime: RFRuntime[F] = null
  def parent: Option[ObjectId]
  def own_children: MIter
  def get_runtime: RFRuntime[F] = runtime
}

case class ObjectProxy(id: ObjectId, dtype: TPE.DataType, parent: Option[ObjectId])

//trait PrimitiveInstance[_F[+_]] extends RFObject[_F] {
//  override type DType = TPE.PrimitiveDataType
//  def value: Value
//  def update_value(v: Value): F[Unit]
//  override def own_children: MIter =
//    MonadicIterator.empty[F, RFObject[F]](mt)
//}

case class RecordFieldInstance(ftype: TPE.RecordFieldType, value: Value)

trait RecordInstance[_F[+_]] extends RFObject[_F] {
  override type DType = TPE.RecordType
  def fields: MonadicIterator[F, RecordFieldInstance]

  private[runtime] def set_field(name: String, value: Value): F[Unit]
}

case class ArrayElementInstance[_F[+_]](index: Int, instance: ObjectId)

trait ArrayInstance[_F[+_]] extends RFObject[_F] {
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
    implicit val mt: MonadThrow[F] = runtime.mt
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
//        case v: PrimitiveInstance[F] => new DebugNode(v)
        case rec: RecordInstance[F] => new RecordObjectAsNode(rec, name.getOrElse(rec.dtype.symbol))
        case array: ArrayInstance[F] => new ArrayObjectAsNode(array, name.getOrElse(""))
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
//        case x: PrimitiveInstance[_] => x.value
        case x: RecordInstance[_] => Value.empty
        case x: ArrayInstance[_] => Value.empty
      }

      override def children: MonadicIterator[F, TreeNode[F]] =
        MonadicIterator.empty(runtime.mt)
      override def name: String = s"${get_value.get[String]}"
      override def icon: Option[String] = Some("fa fa-bug")
      override def actions: Seq[Action] = Seq()
    }

    class RecordObjectAsNode(val obj: RecordInstance[F], val name: String) extends ObjectAsNode[RecordInstance[F]] {
      import Value.implicits._
      override def icon: Option[String] = Some("fa fa-map")
      override def actions: Seq[Action] = Seq()
      override def children: MonadicIterator[F, TreeNode[F]] =
        MonadicIterator.from_fiterator(obj.fields.traverse(flist =>
          flist.filter(fi => fi.ftype.dtype match {
            case x: CompoundDataType => true
            case x: ArrayType => true
            case _ => false
          }).map(x => (x.ftype, x.value.get[ObjectId].get)).iterator
        ))
        .flat_map(x => {
            as_nodeF(x._2, Some(x._1.make_descr))
        })
    }

    class ArrayObjectAsNode(val obj: ArrayInstance[F], val name: String) extends ObjectAsNode[ArrayInstance[F]] {
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
    class RecordObjectAsPropSource[F[+_]](val obj: RecordInstance[F]) extends PropertySource[F] {
      override val id: ObjectId = obj.id
      private var _dispatcher = new Dispatcher[F] {
        implicit val mt: MonadThrow[F] = obj.runtime.mt
        override def dispatch(action: RuntimeAction): F[Unit] =
          obj.runtime.dispatch(action)
        override def select(id: ObjectId): F[Unit] = mt.pure()
      }
      override def props: MonadicIterator[F, Property] =
        obj.fields
          .map {field => FieldProperty(field)
//            val value_f: F[Value] = mt.pure(field.value)

//            mt.map(value_f)(v => new Property {
////              override def id: ObjectId = field.value
//              override def name: String = field.ftype.name
//              override def descr: String = field.ftype.make_descr
//              override def dtype: DataType = field.ftype.dtype
//              override def value: Value = v
//            })
          }

      override implicit val mt: MonadThrow[F] = obj.runtime.mt
      override def dispatcher: Dispatcher[F] = _dispatcher
    }

    class OtherAsPropSource[F[+_]](val obj: RFObject[F]) extends PropertySource[F] {
      override val id: ObjectId = obj.id
      override implicit val mt: MonadThrow[F] = obj.runtime.mt
      override def props: MonadicIterator[F, Property] = MonadicIterator.empty
      override def dispatcher: Dispatcher[F] = new Dispatcher[F] {
        override implicit val mt: MonadThrow[F] = obj.runtime.mt
        override def select(id: ObjectId): F[Unit] = mt.pure()
        override def dispatch(action: RuntimeAction): F[Unit] = ???
      }
    }

    def apply[F[+_]](obj: RFObject[F]): PropertySource[F] = obj match {
//      case x: PrimitiveInstance[F] => new OtherAsPropSource(x)
      case rec: RecordInstance[F] => new RecordObjectAsPropSource(rec)
      case array: ArrayInstance[F] => new OtherAsPropSource(array)
    }
  }


  object TableView {

    import Value.implicits._
    import sysmo.reform.shared.table.TableService

    class ArrayObjectAsTableView[_F[+_]](val array: ArrayInstance[_F]) extends TableService[_F] {
      private val runtime = array.runtime
      override implicit val mt: MonadThrow[F] = runtime.mt

      override def list_tables(): F[Seq[Schema]] =
        array.dtype.prototype match {
        case recordType: RecordType => mt.pure(Seq(recordType))
        case unionType: UnionType => mt.pure(unionType.subtypes)
      }

      override def table_schema(table_id: String): F[Schema] = {
        array.dtype.prototype match {
          case recordType: RecordType => mt.pure(recordType)
          case unionType: UnionType => unionType.subtypes
            .find(t => t.symbol == table_id) match {
            case Some(t) => mt.pure(t)
            case None => mt.raiseError(
              new NoSuchElementException(
                s"No such type $table_id in ${unionType.symbol} subtypes"
              )
            )
          }
        }
      }

      override def query_table(q: Query): F[Table[F]] = {
        // TODO arbitrary argument
        mt.map(table_schema(""))(sch => {
          new Table[F] {
            override implicit val mt: MonadThrow[F] = runtime.mt
            override def schema: Schema = sch
            override def nrow: F[Int] = runtime.count(q)
            override def row_iter: MonadicIterator[F, Table.Row] = {
              array.elements
                .flat_map(element => runtime.get(element.instance))
                .flat_map {
                  case rec: RecordInstance[F] => rec.fields.map(
                    field => field.value
                  ).traverse()
                  case x => mt.raiseError(new IllegalArgumentException(s"Expected array element to be record, found ${x.dtype}"))
                }.map(values => {
                  new Table.Row {
                    override def schema: Schema = sch
                    override protected def _get(col: Int): Value = values(col)
                  }
                })
            }
          }
        })
      }

    }

    def apply[F[+_]](obj: ArrayInstance[F]): ArrayObjectAsTableView[F] =
      new ArrayObjectAsTableView(obj)
  }
}