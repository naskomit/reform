package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.{tplight => TP}

case class GroupUnion(vertex: TP.Vertex) extends AbstractGroup {
  override type ED = GroupUnion.Def.type
  override val ed = GroupUnion.Def
  override def symbol: String = get(_.symbol).get
  def subtypes: Seq[FieldGroup] = {
    vertex.vertices(TP.Direction.OUT, Seq(HasPrototype.Def.label))
      .map {
        case v if v.label == FieldGroup.Def.label => new FieldGroup(v)
        case _ => throw new IllegalStateException()
      }.toSeq
  }

  def supertype_of(g: FieldGroup): Boolean = {
    vertex.vertices(TP.Direction.OUT, Seq(HasPrototype.Def.label))
      .contains(g.vertex)
  }
}

object GroupUnion extends FormElementCompanion[GroupUnion] {
  object Def extends IDef {
    val label = "GroupUnion"

    object props extends Props {
      val symbol: GO.Property[String] = GO.Property[String]("symbol")
    }
  }
  class Builder(val graph: TP.Graph, symbol: String) extends IBuilder {
    set_prop(_.symbol, symbol)
    def | (builder: FieldGroup.Builder): this.type = {
      new HasPrototype.Builder(graph, vertex, builder.vertex)
      this
    }

    def build: GroupUnion = new GroupUnion(vertex)
  }

  implicit val _cmp: FormElementCompanion[GroupUnion] = this
  def builder(graph: TP.Graph, symbol: String): Builder = new Builder(graph, symbol)
}