package sysmo.reform.shared.data

import scala.reflect.ClassTag

trait Record extends Equals

sealed trait FieldType
case class StringType() extends FieldType
case class IntegerType() extends FieldType

sealed trait Domain

case class EnumeratedOption(value: String, label: String)
case class EnumeratedDomain(options: Seq[EnumeratedOption]) extends Domain {
}
object EnumeratedDomain {
  def apply[X: ClassTag](options: Seq[String]): EnumeratedDomain =
    EnumeratedDomain(options.map(x => EnumeratedOption(x, x)))

}

case class RecordField(name: String, label: String, tpe: FieldType, domain: Option[Domain] = None)

trait RecordMeta[U] {
  type FieldKey
  val field_keys: Seq[FieldKey]
  val fields : Map[FieldKey, RecordField]

  def field_key(name : String): FieldKey

  def check_field_type[FT](key : FieldKey, value : Any) : FT = {
    value match {
      case x : FT => x
      case _ => throw new IllegalArgumentException(f"Value $value does not correspond to type ${fields(key).tpe} of field $key")
    }
  }

  def ensure_type(obj : Any): U = {
    obj match {
      case x : U => x
      case _ => throw new IllegalArgumentException(f"Object $obj is not of correct type")
    }
  }

  def get_value(obj: U, key : FieldKey): Any

//  def get_value_any(obj: Any, key : FieldKey): Any =
//    get_value(ensure_type(obj), key)

  def update_value(obj : U, key : FieldKey, value : Any): U

//  def update_value_any(obj : Any, key : FieldKey, value : Any): U =
//    update_value(ensure_type(obj), key, value)
}

trait RecordWithMeta[U] {
  val _meta: RecordMeta[U]
}


