package sysmo.reform.shared.chart

import sysmo.reform.shared.data.{EnumeratedDomain, NoFilter, OptionFilter, OptionProvider, RecordField, RecordMeta, RecordWithMeta, StringType}

import scala.collection.immutable.VectorMap
import scala.concurrent.{ExecutionContext, Future}

//Distribution(data_id: String, column_id: String)
object DistributionMeta {
  object FieldEnum extends Enumeration {
    val data_id, column_id = Value
  }
}

class DistributionMeta(val option_provider: OptionProvider[DistributionSettings]) extends RecordMeta[DistributionSettings] {
  val FieldEnum = DistributionMeta.FieldEnum
  val id = "Distribution"
  override type FieldKey = FieldEnum.Value

  override val field_keys = Seq(FieldEnum.data_id, FieldEnum.column_id)

  override val fields = VectorMap(
    FieldEnum.data_id -> RecordField(name = "data_id", label = "Data",
      tpe = StringType(), domain = None),
    FieldEnum.column_id -> RecordField(name = "column_id", label = "Column",
      tpe = StringType(), domain = None)
  )

  override def field_key(name: String): FieldKey = FieldEnum.withName(name)

  override def get_value(obj: DistributionSettings, key: FieldKey): Any = {
    key match {
      case FieldEnum.data_id => obj.data_id
      case FieldEnum.column_id => obj.column_id
    }
  }

  override def update_value(obj: DistributionSettings, key: FieldKey, value: Any): DistributionSettings = {
    key match {
      case FieldEnum.data_id => obj.copy(data_id = check_field_type[String](key, value))
      case FieldEnum.column_id => obj.copy(column_id = check_field_type[String](key, value))
    }
  }

  override def update_options(obj: DistributionSettings, key: FieldKey, flt: OptionFilter)(implicit ec: ExecutionContext): Future[Unit] = {
    option_provider.get(obj, key.toString, NoFilter).map(opts => {
      fields(key).domain = Some(EnumeratedDomain(opts))
    })
  }

  def on_change(obj: DistributionSettings, key: FieldKey)(implicit ec: ExecutionContext): Future[Unit] = key match {
    case FieldEnum.data_id => update_options(obj, FieldEnum.column_id, NoFilter)
  }
}

