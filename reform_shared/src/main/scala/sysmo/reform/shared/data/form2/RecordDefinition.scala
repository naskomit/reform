package sysmo.reform.shared.data.form2

import sysmo.reform.shared.util.{INamed, Named}

import scala.collection.mutable

/** Record definition elements */

sealed trait FieldType extends Product with Serializable
case object Int extends FieldType
case object Real extends FieldType
case object Bool extends FieldType
case object Char extends FieldType
case object Categorical extends FieldType
case object Date extends FieldType
case class ValueArray(tpe: FieldType) extends FieldType

trait PresentationType extends Product with Serializable

case class FixedOptions(option: Named*)

trait Field extends INamed with Product with Serializable

case class ValueField(name: String, label: Option[String], tpe: FieldType) extends Field
case class FieldArray(name: String, label: Option[String], tpe: Field) extends Field
case class RecordField(name: String, label: Option[String], tpe: RecordDefinition) extends Field
case class RecordArray(name: String, label: Option[String], tpe: RecordField) extends Field

case class RecordDefinition(name: String, label: Option[String], fields: Seq[Field]) extends INamed {
  def drop_fields(dfields: String*): RecordDefinition = {
    val dfields_set: Set[String] = dfields.toSet
    RecordDefinition(name, label, fields.filterNot(f => dfields_set.contains(f.name)))
  }
}

object RecordDefinition {
  class Builder(name: String) {
    var _label = Option[String](null)
    val fields = mutable.ArrayBuffer[Field]()
    def label(v: String): this.type = {
      _label = Some(v)
      this
    }
    def field(v: Field): this.type = {
      fields += v
      this
    }
    def build: RecordDefinition =
      RecordDefinition(name, _label, fields.toSeq)
  }

  def builder(name: String) = new Builder(name)
}


/** Form layout elements */

case class FieldRef(name: String)

trait FormLayout
case class SequentialLayout(groups: FormLayout*) extends FormLayout

case class FieldGroupLayout(
  name: String, label: Option[String], fields: Seq[FieldRef]
) extends FormLayout with INamed

case class RecordLayout(
 name: String, label: Option[String], field: FieldRef
) extends FormLayout with INamed

case class RecordArrayLayout(
 name: String, label: Option[String], field: FieldRef
) extends FormLayout with INamed
//trait FieldGroupLayout extends FormLayout
//trait ArrayLayout extends FormLayout

case class FormDefiniton(
  name: String,
  label: Option[String],
  rec: RecordDefinition,
  layout: FormLayout
) extends INamed

