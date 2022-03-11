package sysmo.reform.services

import autowire.Core.Request
import io.circe.Json
import io.circe.syntax._
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.data.{table => dt}
import sdt.Transport._
import Q.TransportCirce._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TableApiServer
  (base_path: Seq[String], app_storage: GraphAppStorage)
  (implicit ec: ExecutionContext) {

    type PickleType = Json
    private val route_map = Map(
      (base_path :+ "row_count") -> this.row_count _,
      (base_path :+ "query_table") -> this.query_table _
    )

    def row_count(args: Map[String, PickleType]): Try[PickleType] = {
      val result = 11
      Success(result.asJson)
    }

    def query_table(args: Map[String, PickleType]): Try[PickleType] = {
      args("query").as[Q.Query].toTry.flatMap(q => {
        try {
          // dt.arrow.create_table_manager
          sdt.with_table_manager()(tm => {
            val result = app_storage.query_table(q, tm)
            Success(result.asJson)
          })
        } catch {
          case e: Throwable => Failure(e)
        }
      })
    }


    val routes: autowire.Core.Router[PickleType] = {
      case Request(path, args) => {
        route_map.get(path)
          .toRight(new RuntimeException(f"Cannot find method for path $path"))
          .toTry
          .flatMap(method => method(args)) match {
          case Success(v) => Future.successful(v)
          case Failure(e) => Future.failed(e)
        }
      }
    }

}
