package sysmo.reform.managers

import sysmo.reform.data.StreamingRecordManager
import sysmo.reform.services.{ChartService, ServerChartService}
import sysmo.reform.shared.chart.{ChartRequest, ChartSettings, DistributionMeta, DistributionSettings}
import sysmo.reform.shared.data.{EnumeratedOption, OptionFilter, OptionProvider, Record, RecordMeta, RecordWithMeta, table => sdt}
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.shared.{query => Q}

import scala.concurrent.Future

trait ChartManager[U <: ChartSettings] {
  val chart_service: ChartService = ServerChartService
  val rec_mngr: StreamingRecordManager[U]
  def option_provider: OptionProvider[U] = settings_meta.option_provider
  val settings_meta: RecordMeta[U]
  def request(): ChartRequest
}

class DistributionChartManager
(val rec_mngr: StreamingRecordManager[Ch.DistributionSettings],
 val settings_meta: RecordMeta[Ch.DistributionSettings]
)
  extends ChartManager[Ch.DistributionSettings] {
  def request(): ChartRequest = {
    val settings = rec_mngr.state
    ChartRequest(
      Map(
        "Data" -> Ch.QuerySource(Q.BasicQuery(
          Q.SingleTable(settings.data_id), Some(Seq(Q.ColumnRef(settings.column_id)))
        ))
      ),
      Seq(Ch.DistributionSettings("Data", settings.column_id))
    )
  }

}

object ChartManager {
  def distribution(default: Ch.DistributionSettings, table_schemas: Map[String, sdt.Schema])(implicit meta_holder: RecordWithMeta[Ch.DistributionSettings]): ChartManager[Ch.DistributionSettings] = {
    val opt_provider = new OptionProvider[Ch.DistributionSettings] {
      override def get(record: DistributionSettings, field_id: String, flt: OptionFilter): Future[Seq[EnumeratedOption]] = {
        DistributionMeta.FieldEnum.withName(field_id) match {
          case DistributionMeta.FieldEnum.column_id => Future.successful(
            table_schemas.toSeq.map(x => EnumeratedOption(x._1, x._1))
          )
          case DistributionMeta.FieldEnum.data_id => Future.successful({
            val schema = table_schemas(record.data_id)
            schema.fields.map(x => EnumeratedOption(x.name, x.label.getOrElse(x.name)))
          })
        }
      }
    }
    val meta = meta_holder._meta(opt_provider)
    new DistributionChartManager(StreamingRecordManager[Ch.DistributionSettings](default), meta)
  }

}