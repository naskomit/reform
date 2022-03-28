package sysmo.reform.shared.data

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


sealed trait FieldValue[+V] extends Equals
case class NoValue[V]() extends FieldValue[V]
case class AllValues[V]() extends FieldValue[V]
case class SomeValue[V](v: V) extends FieldValue[V]
case class MultiValue[V](v: Seq[V]) extends FieldValue[V]

trait Record extends Equals
object Record {
  type ValueMap = Map[String, FieldValue[_]]
}

sealed trait FieldType
case class StringType() extends FieldType
case class IntegerType() extends FieldType
case class RealType() extends FieldType
case class BoolType() extends FieldType

sealed trait Domain

case class EnumeratedOption(value: String, label: String)
case class EnumeratedDomain(options: Seq[EnumeratedOption]) extends Domain {
}

object EnumeratedDomain {
  def apply[X: ClassTag](options: Seq[String]): EnumeratedDomain =
    EnumeratedDomain(options.map(x => EnumeratedOption(x, x)))
}

case class EnumeratedDomainSource[-U](option_provider: OptionProvider, field_id: String) extends Domain {
  def get(record: Record.ValueMap, flt: OptionFilter): Future[Seq[EnumeratedOption]] = {
    option_provider.get(record, field_id, flt)
  }
}

case class RecordField(name: String, label: Option[String], tpe: FieldType, domain: Option[Domain] = None) {
  def make_label: String = label.getOrElse(name)
}


sealed trait OptionFilter
case object NoFilter extends OptionFilter
case class StringFilter(exp: String) extends OptionFilter

trait OptionProvider {
  def get(record: Record.ValueMap, field_id: String, flt: OptionFilter): Future[Seq[EnumeratedOption]]
}

object DummyOptionProvider extends OptionProvider {
  def get(record: Record.ValueMap, field_id: String, flt: OptionFilter): Future[Seq[EnumeratedOption]] =
    Future.successful(Seq())
}

trait FieldOptionProvider {
  def get(flt: OptionFilter): Future[Seq[EnumeratedOption]]
}

trait RecordMeta[U] extends Equals {
  type RecordType = U
  val id: String
  type FieldKey
  val field_keys: Seq[FieldKey]
  val fields : Map[FieldKey, RecordField]
  val option_provider: OptionProvider

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
  def _meta: RecordMeta[U] = _meta(DummyOptionProvider)
  def _meta(option_provider: OptionProvider): RecordMeta[U]
}


