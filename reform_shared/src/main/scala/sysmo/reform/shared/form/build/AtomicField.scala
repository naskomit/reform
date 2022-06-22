package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}

trait AtomicField extends FormElement {
  override def symbol: String = "---"
}

object AtomicField {
  def from_vertex(v: TP.Vertex): Option[AtomicField] = v.label match {
    case StringField.Def.label => Some(new StringField(v))
    case _ => None
  }
}

class StringField(val vertex: TP.Vertex) extends AtomicField {
  override type ED = StringField.Def.type
  override val ed = StringField.Def
}

object StringField extends FormElementCompanion[StringField] {
  object Def extends IDef {
    val label = "StringField"

    object props extends Props {
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: StringField = new StringField(vertex)
  }
  implicit val _cmp: FormElementCompanion[StringField] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}

class BooleanField(val vertex: TP.Vertex) extends AtomicField {
  override type ED = BooleanField.Def.type
  override val ed = BooleanField.Def
}

object BooleanField extends FormElementCompanion[BooleanField] {
  object Def extends IDef {
    val label = "BooleanField"

    object props extends Props {
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: BooleanField = new BooleanField(vertex)
  }
  implicit val _cmp: FormElementCompanion[BooleanField] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}

class IntegerField(val vertex: TP.Vertex) extends AtomicField {
  override type ED = IntegerField.Def.type
  override val ed = IntegerField.Def
}

object IntegerField extends FormElementCompanion[IntegerField] {
  object Def extends IDef {
    val label = "IntegerField"

    object props extends Props {
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: IntegerField = new IntegerField(vertex)
  }
  implicit val _cmp: FormElementCompanion[IntegerField] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}
class FloatField(val vertex: TP.Vertex) extends AtomicField {
  override type ED = FloatField.Def.type
  override val ed = FloatField.Def
}

object FloatField extends FormElementCompanion[FloatField] {
  object Def extends IDef {
    val label = "FloatField"

    object props extends Props {
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: FloatField = new FloatField(vertex)
  }
  implicit val _cmp: FormElementCompanion[FloatField] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}
class SelectField(val vertex: TP.Vertex) extends AtomicField {
  override type ED = SelectField.Def.type
  override val ed = SelectField.Def
  def multiple: Boolean = get(_.multiple).getOrElse(false)
  def min: Option[Int] = get(_.min)
  def max: Option[Int] = get(_.max)
}

object SelectField extends FormElementCompanion[SelectField] {
  object Def extends IDef {
    val label = "SelectField"

    object props extends Props {
      val multiple = GO.Property[Boolean]("multiple", Some(false))
      val min = GO.Property[Int]("min")
      val max = GO.Property[Int]("max")
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: SelectField = new SelectField(vertex)
  }
  implicit val _cmp: FormElementCompanion[SelectField] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}

