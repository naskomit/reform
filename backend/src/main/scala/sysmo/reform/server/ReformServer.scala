package sysmo.reform.server

import cats.MonadThrow
import cats.syntax.all._
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.parser.decode
import io.circe.{Encoder, Decoder}
import io.circe.syntax._
import sysmo.reform.shared.query.{Query, QueryService}
import sysmo.reform.shared.runtime.RFRuntime

trait ReformServer[_F[+_]] {
  type F[+X] = _F[X]
  implicit val mt: MonadThrow[F]
  val config: Config = ConfigFactory.load()
  val config_storage: Config = config.getConfig("storage")

  import sysmo.reform.shared.data.Transport._
  import sysmo.reform.shared.table.Transport._
  import sysmo.reform.shared.types.Transport._
  import sysmo.reform.shared.query.Transport._

  def runtime: RFRuntime[F]
  def query_service: QueryService[F]

  def parse_body[T : Decoder](body: String): F[T] = {
    decode[T](body) match {
      case Left(error) => mt.raiseError(error)
      case Right(value) => mt.pure(value)
    }
  }

  def make_handler[I: Decoder, O: Encoder](f: I => F[O])(body: String): F[String] = {
    parse_body[I](body).flatMap(i => f(i)).map(o => o.asJson.toString)
  }

  def handle_query(body: String): F[String] = {
    make_handler {query : Query =>
      for {
        result_set <- query_service.query_table(query)
        table <- query_service.materialize_result(result_set)
      } yield table
    }(body)
  }
}
