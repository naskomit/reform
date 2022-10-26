package sysmo.reform.shared.runtime

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, RecordFieldType, RecordType}
import sysmo.reform.shared.util.MonadicIterator

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object LocalObjects {
  case class RecordObjectImpl[_F[+_]](dtype: RecordType, id: ObjectId, parent: Option[ObjectId])(implicit val mt: MonadThrow[_F])
    extends RecordInstance[_F] {
    import Value.implicits._
    protected val children: ArrayBuffer[Value] = ArrayBuffer.fill(dtype.fields.size)(Value.empty)
    override def own_children: MIter = {
      val child_fields = children.iterator.zipWithIndex.collect {
        case (value, i) if dtype.fields(i).isInstanceOf[CompoundDataType] =>
          value.get[ObjectId].get
      }
      MonadicIterator.from_iterator[F, ObjectId](child_fields)
    }
    override def fields: MonadicIterator[F, RecordFieldInstance] =
      MonadicIterator.from_iterator[F, (RecordFieldType, Value)](dtype.fields.iterator.zip(children))
        .map{case(ftype, child) => RecordFieldInstance(ftype, child)}

    override private[runtime] def set_field(name: String, value: Value): F[Unit] =
      dtype.field_index(name) match {
        case Some(i) => {
          children(i) = value
          mt.pure()
        }
        case None => mt.raiseError(new NoSuchFieldException(s"$name in Record ${dtype.symbol}"))
      }
  }

  case class ArrayObjectImpl[_F[+_]](dtype: ArrayType, id: ObjectId, parent: Option[ObjectId])(implicit val mt: MonadThrow[_F])
    extends ArrayInstance[_F] {
    protected val children: mutable.ArrayBuffer[ObjectId] = new mutable.ArrayBuffer()
    override def own_children: MIter = {
      MonadicIterator.from_iterator[F, ObjectId](children.iterator)
    }

    def elements: MonadicIterator[F, ArrayElementInstance[F]] =
      MonadicIterator.from_iterator[F, ArrayElementInstance[F]](
        children.zipWithIndex.map(x => ArrayElementInstance[F](x._2, x._1)).iterator
      )

    private[runtime] def add_element(instance: ObjectId): F[Unit] =
      mt.pure(children.append(instance))
  }

  def record[F[+_]](dtype: RecordType, id: ObjectId, parent: Option[ObjectId])(implicit mt: MonadThrow[F]): RecordInstance[F] =
    new RecordObjectImpl(dtype, id, parent)

  def array[F[+_]](dtype: ArrayType, id: ObjectId, parent: Option[ObjectId])(implicit mt: MonadThrow[F]): ArrayInstance[F] =
    new ArrayObjectImpl(dtype, id, parent)

}
