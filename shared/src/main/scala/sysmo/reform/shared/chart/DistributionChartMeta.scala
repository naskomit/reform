package sysmo.reform.shared.chart

import sysmo.reform.shared.data.Record.ValueMap
import sysmo.reform.shared.data.{EnumeratedDomain, EnumeratedDomainSource, EnumeratedOption, FieldValue, NoFilter, OptionFilter, Record, RecordField, RecordMeta, RecordOptionProvider, RecordWithMeta, SomeValue, StringType, TableDatasource, ValueDependency, ValueDependencyHandler}

import scala.collection.immutable.VectorMap
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

//Distribution(data_id: String, column_id: String)
class DistributionOptionProvider(table_service: TableDatasource)(implicit ec: ExecutionContext) extends RecordOptionProvider {
  override def get(record: ValueMap, field_id: String, flt: OptionFilter): Future[Seq[EnumeratedOption]] = {
    field_id match {
      case "data_id" => table_service.list_tables(NoFilter)
        .map(x => x.map(item => EnumeratedOption(item.name, item.make_label)))
      case "column_id" => {
        record("data_id") match {
          case SomeValue(v: String) => {
            table_service.table_schema(v)
              .map(schema => schema.fields.map(x => EnumeratedOption(x.name, x.label.getOrElse(x.name))))
          }

          case _ => Future.successful(Seq())
        }
      }
    }
  }
}


object DistributionChartMeta {
  object FieldEnum extends Enumeration {
    val data_id, column_id = Value
  }
}

class DistributionChartMeta(val option_provider: RecordOptionProvider)
  extends RecordMeta[DistributionSettings] {

  val FieldEnum = DistributionChartMeta.FieldEnum
  val id = "Distribution"

  override type FieldKey = FieldEnum.Value
  override val field_dependencies = Seq(
    ValueDependency("column_id", Seq("data_id"))
  )

  override def value_map(u: RecordType): Record.ValueMap = {
    Map[String, FieldValue](
      "data_id" -> SomeValue(u.data_id),
      "column_id" -> SomeValue(u.column_id)
    )
  }

  override def validate(c: Record.ValueMap): Either[Map[String, Throwable], RecordType] = {
    val errors = mutable.HashMap[String, Throwable]()

    val v1 = c("data_id") match {
      case SomeValue(v : String) => Some(v)
      case _ => {
        errors("data_id") = new IllegalArgumentException("Incorrect value for 'data_id'")
        None
      }
    }

    val v2 = c("column_id") match {
      case SomeValue(v : String) => Some(v)
      case _ => {
        errors("column_id") = new IllegalArgumentException("Incorrect value for 'column_id'")
        None
      }
    }

    if (errors.isEmpty) {
      Right(DistributionSettings(v1.get, v2.get))
    } else {
      Left(errors.toMap)
    }
  }

  override val field_keys = Seq(FieldEnum.data_id, FieldEnum.column_id)

  override val fields = VectorMap(
    FieldEnum.data_id -> RecordField(name = "data_id", label = Some("Data"),
      tpe = StringType(), domain = Some(EnumeratedDomainSource(option_provider, "data_id"))),
    FieldEnum.column_id -> RecordField(name = "column_id", label = Some("Column"),
      tpe = StringType(), domain = Some(EnumeratedDomainSource(option_provider, "column_id")))
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

}

