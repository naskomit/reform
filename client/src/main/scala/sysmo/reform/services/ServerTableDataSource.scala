package sysmo.reform.services

import autowire.Core.Request
import io.circe.syntax._
import sysmo.reform.shared.{data => D}
import sysmo.reform.shared.data.table.Schema
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.data.{TableService, table => sdt}
import sysmo.reform.shared.util.{Named, NamedValue}

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import sysmo.reform.util.log.Logging

// TODO this is specific
class ServerTableDataSource(schemas: NamedValue[Schema]*) extends TableService with Logging {
  import sdt.Transport._
  import Q.Transport._

  val api_client = ApiClient("api/table")
  val base_path: Seq[String] = Seq("sysmo", "reform", "services", "TableDataService")

  override def list_tables(optionFilter: D.OptionFilter): Future[Seq[Named]] = {
    Future(schemas
      .map(x => Named(x.name, x.label))
      .filter(x => optionFilter match {
        case D.NoFilter => true
        case D.LabelFilter(label) => x.make_label.contains(label)
        case D.ValueFilter(v) => x.name == v
      }

      ).toSeq)
  }

  override def table_schema(table_id: String): Future[Schema] = {
    schemas.find(x => x.name == table_id) match {
      case Some(NamedValue(_, _, schema)) => Future.successful(schema)
      case None => Future.failed(new IllegalArgumentException(s"No table $table_id found"))
    }
  }

  //  override def row_count: Future[Int] = {
//    logger.info("row_count")
//    api_client.doCall(Request(
//      base_path :+ "row_count",
//      Map()
//    )).map(x => x.as[Int].getOrElse(throw new RuntimeException(f"Expected integer, got $x")))
//  }

  override def query_table(q : Q.Query): RemoteBatch = {
    logger.info("run_query")
    val q_json = q.asJson
    logger.info(q_json)

    val resp = api_client.doCall(Request(
      base_path :+ "query_table",
      Map("query" -> q_json)
    ))
    resp.map(x => x.as[sdt.Table] match {
      case Left(err) =>  throw new RuntimeException(f"Expected Table , got $x")
      case Right(v) => v
    })
  }
}
