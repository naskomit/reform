package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.{tplight => TP}

case class GroupArray(vertex: TP.Vertex) extends FormElement {
  override type ED = GroupArray.Def.type
  override val ed = GroupArray.Def
  def prototype: AbstractGroup = vertex.edges(TP.Direction.OUT, Seq(HasPrototype.Def.label))
    .find(e => e.label == HasPrototype.Def.label)
    .map(e => FormElement.from_vertex(e.in_vertex).get).get.asInstanceOf[AbstractGroup]
  def prototype_rel: HasPrototype = vertex.edges(TP.Direction.OUT, Seq(HasPrototype.Def.label))
    .find(e => e.label == HasPrototype.Def.label).map(e => HasPrototype(e)).get
  override def symbol: String = "---"
}

object GroupArray extends FormElementCompanion[GroupArray] {
  object Def extends IDef {
    val label = "GroupArray"

    object props extends Props {
//      val symbol: GO.Property[String] = GO.Property[String]("symbol")
    }
  }
  class Builder(val graph: TP.Graph) extends IBuilder {

    def build: GroupArray = new GroupArray(vertex)
  }

  implicit val _cmp: FormElementCompanion[GroupArray] = this
  def builder(graph: TP.Graph): Builder = new Builder(graph)
}