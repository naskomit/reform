package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.{tplight => TP}

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


