package sysmo.reform.shared.data

import sysmo.reform.shared.data.Record.ValueMap
import sysmo.reform.shared.util.LabeledValue

import scala.concurrent.Future
import scala.reflect.ClassTag




sealed trait FieldValue
case object NoValue extends FieldValue
case object AllValues extends FieldValue
case class SomeValue[+V](v: LabeledValue[V]) extends FieldValue
case class MultiValue[+V](v: Seq[LabeledValue[V]]) extends FieldValue

trait Record extends Equals
object Record {
  type ValueMap = Map[String, FieldValue]
}

sealed trait FieldType
case class StringType() extends FieldType
case class IntegerType() extends FieldType
case class RealType() extends FieldType
case class BoolType() extends FieldType

sealed trait Domain

case class EnumeratedDomain(options: Seq[LabeledValue[_]]) extends Domain {
}

object EnumeratedDomain {
  def apply[X: ClassTag](options: Seq[String]): EnumeratedDomain =
    EnumeratedDomain(options.map(x => LabeledValue(x, Some(x))))
}

case class EnumeratedDomainSource[-U](option_provider: RecordOptionProvider, field_id: String) extends Domain

case class RecordField(name: String, label: Option[String], tpe: FieldType, domain: Option[Domain] = None) {
  def make_label: String = label.getOrElse(name)
}


sealed trait OptionFilter
case object NoFilter extends OptionFilter
case class LabelFilter(exp: String) extends OptionFilter
case class ValueFilter(v: Any) extends OptionFilter

trait RecordOptionProvider {
  def get_options(record: Record.ValueMap, field_id: String, flt: OptionFilter): Future[Seq[LabeledValue[_]]]
  def label_values(record: Record.ValueMap): Future[ValueMap]
}

object DummyRecordOptionProvider$ extends RecordOptionProvider {
  def get_options(record: Record.ValueMap, field_id: String, flt: OptionFilter): Future[Seq[LabeledValue[_]]] =
    Future.successful(Seq())

  override def label_values(record: ValueMap): Future[ValueMap] =
    Future.successful(record)
}

trait FieldOptionProvider {
  def get(flt: OptionFilter): Future[Seq[LabeledValue[_]]]
}

trait Interdependency
trait ValueDependencyHandler {
  def value(v: Record.ValueMap) = NoValue
  def choices(v: Record.ValueMap)
}
case class ValueDependency(sink: String, sources: Seq[String]) extends Interdependency
case class ActivationDependency(sink: String, sources: Seq[String]) extends Interdependency
case class VisibilityDependency(sink: String, sources: Seq[String]) extends Interdependency

trait RecordMeta[U] extends Equals {
  type RecordType = U
  val id: String
  type FieldKey
  val field_keys: Seq[FieldKey]
  val fields : Map[FieldKey, RecordField]
  val option_provider: RecordOptionProvider
  val field_dependencies: Seq[Interdependency] = Seq()

  def value_map(u: RecordType): Record.ValueMap
  def validate(c: Record.ValueMap): Either[Map[String, Throwable], RecordType]

  def field_key(name : String): FieldKey

  def check_field_type[FT](key : FieldKey, value : Any)(implicit _t : ClassTag[FT]) : FT = {
    value match {
      case x : FT => x
      case _ => throw new IllegalArgumentException(f"Value $value does not correspond to type ${fields(key).tpe} of field $key")
    }
  }

  def ensure_type(obj : Any)(implicit _t : ClassTag[U]): U = {
    obj match {
      case x : U => x
      case _ => throw new IllegalArgumentException(f"Object $obj is not of correct type")
    }
  }

  def get_value(obj: U, key : FieldKey): Any

  def update_value(obj: U, key : FieldKey, value : Any): U

//  def update_options(obj: U, key : FieldKey, flt: OptionFilter)(implicit ec: ExecutionContext): Future[Unit]

  override def canEqual(that: Any): Boolean = that.isInstanceOf[RecordMeta[U]]

  override def equals(o: Any): Boolean = o match {
    case x: RecordMeta[U] => field_keys.equals(x.field_keys) && fields.equals(x.fields)
    case _ => false
  }
}


trait RecordWithMeta[U] {
  def _meta: RecordMeta[U] = _meta(DummyRecordOptionProvider$)
  def _meta(option_provider: RecordOptionProvider): RecordMeta[U]
}


