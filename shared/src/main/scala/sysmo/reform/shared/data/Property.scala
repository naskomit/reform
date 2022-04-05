package sysmo.reform.shared.data

import sysmo.reform.shared.data.form.RecordOptionProvider
import sysmo.reform.shared.util.{LabeledValue, TNamed}

import scala.reflect.ClassTag

sealed trait PropType
case class StringType() extends PropType
case class IntegerType() extends PropType
case class RealType() extends PropType
case class BoolType() extends PropType
case class DateType() extends PropType
case class DateTimeType() extends PropType

sealed trait Domain

case class CategoricalDomain(options: Option[Seq[LabeledValue[_]]]) extends Domain {
}

//object CategoricalDomain {
//  def apply[X: ClassTag](options: Seq[_]): CategoricalDomain =
//    CategoricalDomain(Some(options.map(x => LabeledValue(x, Some(x.toString)))))
//}

case class CategoricalDomainSource[RecClass, RetContainer[_]](option_provider: OptionProvider[RecClass, RetContainer], field_id: String) extends Domain

sealed trait OptionFilter
case object NoFilter extends OptionFilter
case class LabelFilter(exp: String) extends OptionFilter
case class ValueFilter(v: Any) extends OptionFilter

trait OptionProvider[RecClass, RetContainer[_]] {
  def get_options(record: RecClass, field_id: String, flt: OptionFilter): RetContainer[Seq[LabeledValue[_]]]
  def label_values(record: RecClass): RetContainer[RecClass]
}

case class Property
(
  name: String, label: Option[String],
  tpe: PropType,
  multiplicity: Int,
  domain: Option[Domain]
) {
  def make_label: String = label.getOrElse(name)
}

object Property {
  class Builder(name: String, tpe: PropType) {
    private var prop = Property(
      name = name, label = None, tpe = tpe,
      multiplicity = 1, domain = None
    )
    def label(value: String): Builder = {
      prop = prop.copy(label = Some(value))
      this
    }

    def mult(value: Int): Builder = {
      prop = prop.copy(multiplicity = value)
      this
    }
    def categorical: Builder = {
      prop = prop.copy(domain = Some(CategoricalDomain(None)))
      this
    }

    def categorical(items: Any*): Builder = {
      val categories = items.map {
        case x: LabeledValue[_] => x
        case x => LabeledValue(x, Some(x.toString))
      }
      prop = prop.copy(domain = Some(CategoricalDomain(Some(categories))))
      this
    }

    def categorical_source[RecClass, RetContainer[_]](source: OptionProvider[RecClass, RetContainer], field_id: String): Builder = {
      prop = prop.copy(domain = Some(CategoricalDomainSource(source, field_id)))
      this
    }

    def build: Property = {
      // check
      prop
    }



  }

  def int(name: String) = new Builder(name, IntegerType())
  def real(name: String) = new Builder(name, RealType())
  def bool(name: String) = new Builder(name, BoolType())
  def string(name: String) = new Builder(name, StringType())
  def date(name: String) = new Builder(name, DateType())
  def datetime(name: String) = new Builder(name, DateTimeType())
  //  def ref(name: String, to: EntitySchema) = new Builder(name, RefType(to))
}