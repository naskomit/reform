package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.{expr => E}

case class Reference(vertex: TP.Vertex) extends FormElement {
  override type ED = Reference.Def.type
  override val ed: ED = Reference.Def
  override def symbol: String = "-->"

  def multiple: Boolean = get(_.multiple).get
  def id_field: String = get(_.id_field).get
  def label_expr: Option[E.Expression] = get(_.label_expr)
  def prototype: FormElement = vertex.edges(TP.Direction.OUT, Seq(HasPrototype.Def.label))
    .find(e => e.label == HasPrototype.Def.label)
    .map(e => FormElement.from_vertex(e.in_vertex).get).get
}

object Reference extends FormElementCompanion[Reference] {
  object Def extends IDef {
    val label = "Reference"

    object props extends Props {
      val multiple = GO.Property[Boolean]("multiple", Some(false))
      val id_field = GO.Property[String]("id_field")
      val label_expr = GO.Property[E.Expression]("label_expr")
    }
  }

  class Builder(val graph: TP.Graph, val prototype: FieldGroup.Builder, id_field: String) extends IBuilder {
    set_prop(_.id_field, id_field)
    def multiple(flag: Boolean = true): this.type = {
      set_prop(_.multiple, flag)
      this
    }
    def label_expr(v: E.Expression): this.type = {
      set_prop(_.label_expr, v)
      this
    }
    def build: Reference = new Reference(vertex)
  }

  class BuilderSource(val graph: TP.Graph) {
    def apply(prototype: FieldGroup.Builder, id_field: String): Builder =
      new Builder(graph, prototype, id_field)
  }
//  def builder(graph: TP.Graph, prototype: FieldGroup.Builder): Builder = new Builder(graph, prototype)
}
