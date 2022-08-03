package sysmo.reform.shared.table2

import sysmo.reform.shared.query.Query
import sysmo.reform.shared.util.Named
import cats.MonadThrow
import sysmo.reform.shared.field.RecordType

trait TableService[U[_]] {
  type MT[T] = U[T]
  val mt: MonadThrow[U]
  type TableIdType
//  def row_count : Future[Int]
  def list_tables(): MT[Seq[Named]]
  def table_schema(table_id: TableIdType): MT[RecordType[_]]
  def table_schema(table_id: String): MT[RecordType[_]]
  def query_table(q : Query): MT[Table]

//  def test(): Unit = {
//    mt.map(query_table())(x => x)
//    mm.handleError(list_tables()) ()
//  }
}

//import scala.concurrent._
//class S1 extends TableService[Future] {
//  import cats.instances.future._
//  import scala.concurrent.duration._
//  import scala.concurrent.ExecutionContext.Implicits.global
//  override val mm = implicitly[MonadThrow[Future]]
//
//  override type TableIdType = this.type
//
//  override def list_tables(): Future[Seq[Named]] = ???
//
//  override def table_schema(table_id: S1.this.type): Future[_][RecordType[_]] = ???
//
//  override def table_schema(table_id: String): Future[_][RecordType[_]] = ???
//
//  override def query_table(q: Query): RemoteBatch = ???
//}