package sysmo.reform.shared.chart

import io.circe.Json
import sysmo.reform.shared.data.{RecordOptionProvider, RecordMeta, RecordWithMeta, Record}
import scala.collection.mutable
import sysmo.reform.shared.util.NamedValue
import sysmo.reform.shared.{query => Q}

/** Request */
sealed trait ChartData
case class QuerySource(q: Q.Query) extends ChartData

trait ChartSettings extends Record

sealed trait ChartDefinition
case class DistributionSettings(data_id: String, column_id: String) extends ChartDefinition with ChartSettings
object DistributionSettings {
  implicit val tometa: RecordWithMeta[DistributionSettings] = new RecordWithMeta[DistributionSettings] {
    override def _meta(option_provider: RecordOptionProvider): RecordMeta[DistributionSettings] =
      new DistributionChartMeta(option_provider)
  }
}
case class ChartRequest(data: Map[String, ChartData], charts: Seq[ChartDefinition])



/** Response */
sealed trait ChartObject
case class Plotly(content: Json) extends ChartObject
case class PlotlyAsText(content: String) extends ChartObject


case class ChartResult(items: Seq[NamedValue[ChartObject]])

object ChartResult {
  class Builder {
    val items = new mutable.ArrayBuffer[NamedValue[ChartObject]]
    def add(item: Product2[String, ChartObject]): this.type = {
      items += NamedValue(item._1, None, item._2)
      this
    }

    def label(value: String): this.type = {
      items.takeRight(1).headOption match {
        case Some(item) => items(items.length - 1) = item.copy(label = Some(value))
        case None =>
      }
      this
    }

    def ++ (result_list: ChartResult): this.type = {
      result_list.items.foreach(item => items += item)
      this
    }

    def build: ChartResult = ChartResult(items.toSeq)
  }

  def builder: Builder = new Builder
}
