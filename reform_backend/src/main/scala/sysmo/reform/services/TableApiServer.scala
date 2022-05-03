package sysmo.reform.services

import io.circe.syntax._
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.data.{table => sdt}
import sdt.Transport._
import Q.Transport._

import scala.concurrent.{ExecutionContext, Future}

class TableApiServer
  (base_path: Seq[String], app_storage: GraphAppStorage)
  (implicit val ec: ExecutionContext) extends APIServer {

    val route_map = Map(
//      (base_path :+ "row_count") ->
//        (_ => row_count().map(_.asJson)),
      (base_path :+ "query_table") ->
        (args => args("query").as[Q.Query].toTry.toFuture
          .flatMap(query_table).map(_.asJson)),
    )

//    def row_count(): Future[Int] = {
//      val result = 11
//      Future.successful(result)
//    }

    def query_table(q: Q.Query): Future[sdt.Table] = {
      try {
        // dt.arrow.create_table_manager
        sdt.with_table_manager()(tm => {
          val result = app_storage.query_table(q, tm)
          Future.successful(result)
        })
      } catch {
        case e: Throwable => Future.failed(e)
      }
    }




}
