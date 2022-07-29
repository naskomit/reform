//package sysmo.reform.shared.chart
//
//import sysmo.reform.shared.data.form.Record.ValueMap
//import sysmo.reform.shared.data.TableService
//import sysmo.reform.shared.data.{CategoricalDomainSource, OptionFilter, LabelFilter, NoFilter, ValueFilter, Property, StringType}
//import sysmo.reform.shared.data.form.{AllValues, FieldValue, MultiValue, NoValue, Record, RecordMeta, RecordOptionProvider, SomeValue, ValueDependency}
//import sysmo.reform.shared.util.LabeledValue
//
//import scala.collection.immutable.VectorMap
//import scala.collection.mutable
//import scala.concurrent.{ExecutionContext, Future}
////Distribution(data_id: String, column_id: String)
//class DistributionOptionProvider(table_service: TableService)(implicit ec: ExecutionContext) extends RecordOptionProvider {
//  override def get_options(record: ValueMap, field_id: String, flt: OptionFilter): Future[Seq[LabeledValue[_]]] = {
//    field_id match {
//      case "data_id" => table_service.list_tables(flt)
//        .map(x => x.map(item => LabeledValue(item.name, item.label)))
//      case "column_id" => {
//        record("data_id") match {
//          case SomeValue(LabeledValue(v: String, _)) => {
//            table_service.table_schema(v)
//              .map(schema =>
//                schema.fields.filter(x => flt match {
//                  case NoFilter => true
//                  case LabelFilter(s) => x.make_label.contains(s)
//                  case ValueFilter(v) => x.name == v
//                }).map(x => LabeledValue(x.name, x.label)))
//          }
//          case _ => Future.successful(Seq())
//        }
//      }
//    }
//  }
//
//  override def label_values(record: ValueMap): Future[ValueMap] = {
//    import cats._
//    import cats.implicits._
//    val ids = record.keys
//    ids.toSeq.traverse(id => {
//      record(id) match {
//        case some_labeled @ SomeValue(LabeledValue(v, Some(_))) => Future.successful(some_labeled)
//        case some_unlabeled @ SomeValue(LabeledValue(v, None)) => get_options(record, id, ValueFilter(v)).map(x => x.headOption match {
//          case Some(LabeledValue(_, label)) => SomeValue(LabeledValue(v, label))
//          case None  => some_unlabeled
//        })
//        case NoValue => Future.successful(NoValue)
//        case AllValues => Future.successful(AllValues)
//        case MultiValue(v) => Future.successful(MultiValue(v))
//      }
//    }).map(values => ids.zip(values).toMap)
//  }
////  {
////    record.map {case(k: String, v: LabeledValue[_]) =>
////      (k, get_options(record, k, StringFilter(v.value.toString)))
////    }.foldLeft()
////    Future.sequence()
////  }
////    record.map(case (k, v) => k match {
////    case "data_id" => get_options(record, k, StringFilter(v)).map(x => x.headOption) match {
////      // If found take the label (not the value, to be on the safe side)
////      case Some(LabeledValue(v1, l)) => LabeledValue(v, l)
////      case None =>
////    }
////  })
//}
//
//
//object DistributionChartMeta {
//  object FieldEnum extends Enumeration {
//    val data_id, column_id = Value
//  }
//}
//
//class DistributionChartMeta(val option_provider: RecordOptionProvider)
//  extends RecordMeta[DistributionSettings] {
//
//  val FieldEnum = DistributionChartMeta.FieldEnum
//  val id = "Distribution"
//
//  override type FieldKey = FieldEnum.Value
//  override val field_dependencies = Seq(
//    ValueDependency("column_id", Seq("data_id"))
//  )
//
//  override def value_map(u: RecordType): Record.ValueMap = {
//    Map[String, FieldValue](
//      "data_id" -> SomeValue(LabeledValue(u.data_id)),
//      "column_id" -> SomeValue(LabeledValue(u.column_id))
//    )
//  }
//
//  override def validate(c: Record.ValueMap): Either[Map[String, Throwable], RecordType] = {
//    val errors = mutable.HashMap[String, Throwable]()
//
//    val v1 = c("data_id") match {
//      case SomeValue(LabeledValue(v : String, _)) => Some(v)
//      case _ => {
//        errors("data_id") = new IllegalArgumentException("Incorrect value for 'data_id'")
//        None
//      }
//    }
//
//    val v2 = c("column_id") match {
//      case SomeValue(LabeledValue(v : String, _)) => Some(v)
//      case _ => {
//        errors("column_id") = new IllegalArgumentException("Incorrect value for 'column_id'")
//        None
//      }
//    }
//
//    if (errors.isEmpty) {
//      Right(DistributionSettings(v1.get, v2.get))
//    } else {
//      Left(errors.toMap)
//    }
//  }
//
//  override val field_keys = Seq(FieldEnum.data_id, FieldEnum.column_id)
//
//
//  override val fields = VectorMap(
//    FieldEnum.data_id -> Property.string("data_id").label("Data").categorical_source(option_provider, "data_id").build,
//    FieldEnum.column_id -> Property.string("column_id").label("Column").categorical_source(option_provider, "column_id").build
//  )
//
//  override def field_key(name: String): FieldKey = FieldEnum.withName(name)
//
//  override def get_value(obj: DistributionSettings, key: FieldKey): Any = {
//    key match {
//      case FieldEnum.data_id => obj.data_id
//      case FieldEnum.column_id => obj.column_id
//    }
//  }
//
//  override def update_value(obj: DistributionSettings, key: FieldKey, value: Any): DistributionSettings = {
//    key match {
//      case FieldEnum.data_id => obj.copy(data_id = check_field_type[String](key, value))
//      case FieldEnum.column_id => obj.copy(column_id = check_field_type[String](key, value))
//    }
//  }
//
//}
//
