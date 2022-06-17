package sysmo.reform.shared.form

import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}

abstract class Field[T <: GO.VertexDef](ed: T) extends FormElement[T](ed) {

}
//extends FormElementCompanion[StringField]
//object Field {
////  trait Builder[T <: Field[_]] extends IBuilder
//
//}

case class StringField(vertex: TP.Vertex) extends Field(StringField.Def) {

}

object StringField extends FormElementCompanion[StringField] {
  object Def extends IDef {
    val label = "StringField"

    object props extends Props {
    }
  }

  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: FET = new StringField(vertex)
  }

  def builder(graph: TP.Graph): Builder = new Builder(graph)

}


