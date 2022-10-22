//package sysmo.reform.storage.orientdb
//
//import cats.MonadThrow
//import sysmo.reform.shared.data.{ObjectId, ObjectIdSupplier, UUIDSupplier, Value}
//import sysmo.reform.shared.runtime.{ArrayElementInstance, ArrayInstance, PrimitiveInstance, Constructors, ObjectProxy, RFObject, RFRuntime, RecordFieldInstance, RecordInstance, RuntimeAction}
//import sysmo.reform.shared.table.Query
//import sysmo.reform.shared.types.{ArrayType, PrimitiveDataType, RecordType}
//import sysmo.reform.shared.util.MonadicIterator
//import sysmo.reform.shared.util.containers.FLocal
//
//class OrientDBRuntime[_F[+_]](session: SessionImpl[_F])(implicit val mt: MonadThrow[_F]) extends RFRuntime[_F]{
//  object RuntimeObjects extends RuntimeObjectsF[F]
//  override protected val objectid_supplier: ObjectIdSupplier = new UUIDSupplier()
//  override def get(id: ObjectId): F[RTO] = ???
//  override def put[T <: RTO](id: ObjectId, obj: T): F[T] = ???
//  override def remove(id: ObjectId): F[Unit] = ???
//  override def list: MonadicIterator[F, ObjectProxy] = ???
//  override def count: F[Int] = ???
//  override def count(q: Query): F[Int] = ???
//  override def run_query(q: Query): MonadicIterator[F, RFObject[F]] = ???
//  override def dispatch(action: RuntimeAction): F[Unit] = ???
//  override val constructors: Constructors[F] = RuntimeObjects.constructors
//}
//
//object OrientDBRuntime {
//
//
//
//
//  import scala.concurrent.{ExecutionContext, Future}
//  def with_futures(storage: StorageImpl[Future]): OrientDBRuntime[Future] = {
//    import scala.concurrent.ExecutionContext.Implicits.global
//    new OrientDBRuntime[Future](storage.session)
//  }
//
//  def sync(storage: StorageImpl[FLocal]): OrientDBRuntime[FLocal] = {
//    new OrientDBRuntime[FLocal](storage.session)
//  }
//}
//
//class RuntimeObjectsF[F[+_]](implicit mt: MonadThrow[F]) {
//  case class PrimitiveInstanceImpl(dtype: PrimitiveDataType, id: ObjectId, value: Value, parent: Option[ObjectId])
//  extends PrimitiveInstance[F] {
//    override implicit val mt: MonadThrow[F] = mt
//    override def update_value(v: Value): F[Unit] = ???
//  }
//
//  case class RecordInstanceImpl(dtype: RecordType, id: ObjectId, parent: Option[ObjectId])
//  extends RecordInstance[F] {
//    override implicit val mt: MonadThrow[F] = mt
//    override def fields: MonadicIterator[F, RecordFieldInstance] = ???
//    override def own_children: MIter = ???
//  }
//
//  case class ArrayInstanceImpl(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId])
//  extends ArrayInstance[F] {
//    override implicit val mt: MonadThrow[F] = mt
//    override def elements: MonadicIterator[F, ArrayElementInstance[F]] = ???
//    override def own_children: MIter = ???
//  }
//
//  object constructors extends Constructors[F] {
//    override def primitive(dtype: PrimitiveDataType, id: ObjectId, value: Value, parent: Option[ObjectId]): F[PrimitiveInstance[F]] =
//      mt.pure(PrimitiveInstanceImpl(dtype, id, value, parent))
//    override def record(dtype: RecordType, id: ObjectId, parent: Option[ObjectId]): F[RecordInstance[F]] =
//      mt.pure(RecordInstanceImpl(dtype, id, parent))
//    override def array(dtype: ArrayType, id: ObjectId, parent: Option[ObjectId]): F[ArrayInstance[F]] =
//      mt.pure(ArrayInstanceImpl(dtype, id, parent))
//  }
//
//}