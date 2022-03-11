package sysmo.coviddata.data

import autowire.Core.Request
import sysmo.reform.shared.data.table.Table
//import autowire._
import io.circe.syntax._
import sysmo.reform.data.TableDatasource
import sysmo.reform.shared.query.Query
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.services.TableApiClient
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue


object DemoServerDataSource extends TableDatasource {
  import sysmo.reform.shared.query.TransportCirce._
  import sdt.Transport._

  val base_path: Seq[String] = Seq("sysmo", "reform", "shared", "data", "TableData")

  override def row_count: Future[Int] = {
    println("DataApiClient / row_count")
    TableApiClient.doCall(Request(
      base_path :+ "row_count",
      Map()
    )).map(x => x.as[Int].getOrElse(throw new RuntimeException(f"Expected integer, got $x")))
  }

  override def query_table(q : Query): RemoteBatch = {
    println("DataApiClient / run_query")
    println(q)

    val resp = TableApiClient.doCall(Request(
      base_path :+ "query_table",
      Map("query" -> q.asJson)
    ))
    resp.map(x => x.as[Table] match {
      case Left(err) =>  throw new RuntimeException(f"Expected Table , got $x")
      case Right(v) => v
    })
  }
}
