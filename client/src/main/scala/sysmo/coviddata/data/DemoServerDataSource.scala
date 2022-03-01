package sysmo.coviddata.data

import autowire.Core.Request
import sysmo.reform.shared.query.{BasicQuery, SingleTable}
//import autowire._
import upickle.default._
import sysmo.coviddata.shared.data.{PatientData, PatientRecord}
import sysmo.reform.data.TableDatasource
import sysmo.reform.shared.query.Query

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import sysmo.reform.util.{PrettyPrint => PP}


//object DataApiClient extends PatientData {
//
//  val doCall = sysmo.reform.services.DataApiClient.doCall
//
//  override def list_patients(): Seq[PatientRecord] = ???
//
//  override def count_patients(): Int = ???
//
//  override def query_gremlin(q: String): Seq[PatientRecord] = ???
//
//  override def query_table(q: Query): Seq[PatientRecord] = ???
//}

object DemoServerDataSource extends TableDatasource[PatientRecord]{
  import sysmo.reform.shared.query.ReadersWriters._

  override def row_count: Future[Int] = {
    println("DataApiClient / row_count")
    val resp = sysmo.reform.services.DataApiClient.doCall(Request(
      List("sysmo", "coviddata", "shared", "data", "PatientData", "count_patients"),
      Map()
    ))
    resp.map(x => read[Int](x))
  }

  override def run_query(q : Query): RemoteBatch = {
    import sysmo.reform.shared.query._
    println("DataApiClient / run_query")
    println(q)
//    val q1 = BasicQuery(
//        SingleTable("PatientData",None,None),
//        Some(QueryFilter(
//          LogicalAnd(List(
//            StringPredicate(StringPredicateOp.Equal, Val("не"),ColumnRef("first_name",None,None)))))),
//            None,
//            Some(QueryRange(0,100)))
//    val q3 = QueryFilter(LogicalAnd(List(
//      StringPredicate(StringPredicateOp.Equal, Val("не"),ColumnRef("first_name",None,None)))))
//    println(q3)
//    println(writeJs(q3))
//    val q2 = Val("не").asInstanceOf[Expression]
//    println(q2)
//    println(write(q2))
//    val bc = q.asInstanceOf[BasicQuery]
//    println(write(bc.filter))
//    println(write(bc.range))
//    println(write(bc.sort))
//    val src = bc.source.asInstanceOf[SingleTable]
//    println(write(src.id))
//    println(write(src.alias))
//    println(write(src.schema))
//    println(write(src.schema))
//    println(write(bc.source))

    val resp = sysmo.reform.services.DataApiClient.doCall(Request(
      List("sysmo", "coviddata", "shared", "data", "PatientData", "query_table"),
      Map("query" -> writeJs(q))
    ))
    resp.map(x => read[Seq[PatientRecord]](x))
//    Future.successful(Seq())
  }
}
