package sysmo.reform.services

import autowire.Core.Request
import sysmo.reform.shared.{chart => Ch}
import io.circe.syntax._
import sysmo.reform.util.log.Logging

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

trait ChartService {
  def chart(cd: Ch.ChartRequest): Future[Ch.ChartResult]
}

object ServerChartService extends ChartService with Logging {
  import Ch.Transport._

  val api_client = ApiClient("api/chart")
  val base_path: Seq[String] = Seq("sysmo", "reform", "services", "ChartService")

  def chart(req: Ch.ChartRequest): Future[Ch.ChartResult] = {
    logger.info("chart")
    val resp = api_client.doCall(Request(
      base_path :+ "chart",
      Map("chart_req" -> req.asJson)
    ))

    resp.map(x => x.as[Ch.ChartResult] match {
      case Left(err) =>  throw new RuntimeException(f"Expected ChartResult , got $x")
      case Right(v) => v
    })
  }
}