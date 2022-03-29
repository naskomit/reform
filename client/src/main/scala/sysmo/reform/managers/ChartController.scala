package sysmo.reform.managers

import sysmo.reform.data.StreamingRecordManager
import sysmo.reform.services.{ChartService, ServerChartService}
import sysmo.reform.shared.chart.{ChartRequest, ChartSettings, DistributionChartMeta, DistributionOptionProvider, DistributionSettings}
import sysmo.reform.shared.data.{EnumeratedOption, OptionFilter, Record, RecordMeta, RecordOptionProvider, RecordWithMeta, SomeValue, TableDatasource, table => sdt}
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.{query => Q}

import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

trait ChartController[U <: ChartSettings] {
  val chart_service: ChartService = ServerChartService
  val rec_mngr: StreamingRecordManager[U]
  def option_provider: RecordOptionProvider = settings_meta.option_provider
  val settings_meta: RecordMeta[U]
  def request(): ChartRequest
}

class DistributionChartController
(val rec_mngr: StreamingRecordManager[Ch.DistributionSettings],
 val settings_meta: RecordMeta[Ch.DistributionSettings]
)
  extends ChartController[Ch.DistributionSettings] {
  def request(): ChartRequest = {
    rec_mngr.validate match {
      case Right(settings) => ChartRequest(
        Map(
          "Data" -> Ch.QuerySource(Q.BasicQuery(
            Q.SingleTable(settings.data_id), Some(Seq(Q.ColumnRef(settings.column_id)))
          ))
        ),
        Seq(Ch.DistributionSettings("Data", settings.column_id))
      )
      // TODO not very useful
      case Left(error) => throw new IllegalStateException("Form is in incorrect state")

    }
  }

}

object ChartController {
  def distribution(default: Ch.DistributionSettings, table_service: TableDatasource)(implicit meta_holder: RecordWithMeta[Ch.DistributionSettings]): ChartController[Ch.DistributionSettings] = {
    val opt_provider = new DistributionOptionProvider(table_service)
    val meta = meta_holder._meta(opt_provider)
    new DistributionChartController(StreamingRecordManager[Ch.DistributionSettings](default), meta)
  }

}