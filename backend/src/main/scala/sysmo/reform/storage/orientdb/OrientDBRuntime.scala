package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import com.orientechnologies.orient.core.id.{ORID}
import com.orientechnologies.orient.core.record.{OElement, ORecord}
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.{ArrayInstance, Constructors, LocalObjects, ObjectProxy, RFRuntime, RecordInstance, RuntimeAction}
import sysmo.reform.shared.table.Table
import sysmo.reform.shared.types.{ArrayType, RecordType, TypeSystem}
import sysmo.reform.shared.util.MonadicIterator
import sysmo.reform.shared.query.{BasicQuery, Query}
import sysmo.reform.shared.{containers => C}

class OrientId(orid: ORID) extends ObjectId {
  override type Id = ORID
  override val v: ORID = orid
  override def show: String = orid.toString
}

object OrientId {
  def apply(orid: ORID): OrientId = new OrientId(orid)
  def apply(db_rec: ORecord): OrientId = OrientId(db_rec.getIdentity)
}

class OrientDBRuntime[_F[+_]](val type_system: TypeSystem, session: SessionImpl[_F])(implicit val mt: MonadThrow[_F])
  extends RFRuntime[_F] {
  override val constructors: Constructors[F] = new Constructors[F] {
    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
      mt.pure(LocalObjects.record(dtype, id, parent))
    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
      mt.pure(LocalObjects.array(dtype, id, parent))
  }


  private object Aux {


    def ensure_not_null[T](x: T, msg: String): F[T] = {
      if (x == null) {
        mt.raiseError(new IllegalArgumentException(msg))
      } else {
        mt.pure(x)
      }
    }

    def load_element(oid: OrientId): F[OElement] = {
      ensure_not_null(session.db_session.load(oid.v): OElement,
        s"Object ${oid} not found in the database")
    }

    def get_parent_id(element: OElement): Option[OrientId] = {
      val parent_id_raw = element.getProperty[ORID]("$$parent_id") match {
        case x => Some(x)
        case null => None
      }
      parent_id_raw.map(x => OrientId(x))
    }

    def get_record_type(element: OElement): F[RecordType] = {
      val class_name = element.getSchemaType.get().getName
      type_system.get(class_name) match {
        case Some(rdt: RecordType) => mt.pure(rdt)
        case None => mt.raiseError(new IllegalArgumentException(
          s"Database class ${class_name} is not found in the runtime type system!"
        ))
      }
    }


  }

  override def get(id: ObjectId): F[RTO] = {
    for {
      oid <- Util.ensure_orientid(id)
      element <- Aux.load_element(oid)
      dtype <- Aux.get_record_type(element)
      instance <- C.catch_exception {
        val parent_id = Aux.get_parent_id(element)
        val inst = LocalObjects.record(dtype, id, parent_id)(mt)
        dtype.fields
          .traverse(field =>
            session.rec_field_codec.read_value(field, element).map(value =>
              inst.set_field(field.name, value)
            )
          ).map(_ => inst)
      }
    } yield instance
  }

  override def create_record(dtype: RecordType, parent: Option[ObjectId]): F[RecordInstance[F]] = {
    val element: OElement = session.db_session.newInstance(dtype.symbol)
    def insert(el: OElement): F[RecordInstance[F]] = {
      val orec: ORecord = el.save()
      constructors.record(dtype, OrientId(orec.getIdentity), parent)
    }
    parent match {
      case Some(p: OrientId) => {
        element.setProperty("$$parent", p.v)
        insert(element)
      }
      case Some(p) => mt.raiseError(
        new IllegalArgumentException("Parent is not an orient ID")
      )
      case None => {
        insert(element)
      }
    }
  }

  override def create_array(dtype: ArrayType, parent: Option[ObjectId]): F[ArrayInstance[F]] = ???

  override def update_record(id: ObjectId, field_values: Seq[(String, Value)]): F[RecordInstance[F]] = {
    for {
      oid <- Util.ensure_orientid(id)
      element <- Aux.load_element(oid)
      rec_type <- Aux.get_record_type(element)
      _ <- field_values.traverse { fv: (String, Value) =>
        val (name, value) = fv
        val field_type = rec_type.field(name)
        field_type match {
          case Some(ftype) => {
            C.catch_exception(
              session.rec_field_codec.write_value(ftype, element, value)
            )
          }
          case None => mt.raiseError(new IllegalArgumentException(
            s"No field ${name} in record type ${rec_type.symbol}"
          ))
        }
      }
      new_rec <- {
        element.save()
        get_typed[RecordInstance[F]](id)
      }
    } yield new_rec
  }

  override def remove(id: ObjectId): F[Unit] = {
    for {
      oid <- Util.ensure_orientid(id)
      res <- C.catch_exception{
        session.db_session.delete(oid.v)
        mt.pure()
      }
    } yield res
  }



  override def list: MonadicIterator[F, ObjectProxy] = ???
  override def count: F[Int] = ???
  override def count(q: Query): F[Int] = ???

  override def run_query(q: Query): F[Table[F]] = {
    for {
      sql <- session.query_service.generate_sql(q)
      table <- session.query_service.run_query(sql)
    } yield table
  }

  override def dispatch(action: RuntimeAction): F[Unit] = ???



}

object OrientDBRuntime {

}