package sysmo.reform.shared.form

import sysmo.reform.shared.form
import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}

case class FieldGroup(vertex: TP.Vertex) extends FormElement(FieldGroup.Def) {

}

object FieldGroup extends FormElementCompanion[FieldGroup] {
  object Def extends IDef {
    val label = "FieldGroup"

    object props extends Props {
      val symbol: GO.Property[String] = GO.Property[String]("symbol")
    }
  }

  class FieldBuilderSource(graph: TP.Graph) {
    def char: StringField.Builder = new StringField.Builder(graph)
    //    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(graph, parent, name)
    //    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(graph, parent, name)
    //    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(graph, parent, name)
    //    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(graph, parent, name)
  }

  class Builder(val graph: TP.Graph, symbol: String) extends IBuilder {
    set_prop(_.symbol, symbol)
    def field(f_rel: HasElement.Builder => HasElement.Builder,
              f_child: FieldBuilderSource => FormElementCompanion[_]#IBuilder): this.type = {
      val child_builder = f_child(new FieldBuilderSource(graph))
      f_rel(new HasElement.Builder(graph, vertex, child_builder.vertex))
      this
    }

    def group(rel_fn: String => Unit): this.type = {

      this
    }

    def build: FET = new FieldGroup(vertex)
  }

  def builder(graph: TP.Graph, symbol: String): Builder = new Builder(graph, symbol)
}

case class HasElement(edge: TP.Edge) extends FormRelation(HasElement.Def) {

}

object HasElement extends FormRelationCompanion[HasElement] {
  object Def extends IDef {
    val label: String = "HasElement"
    object props extends Props {
      val name: GO.Property[String] = GO.Property[String]("name")
      val descr: GO.Property[String] = GO.Property[String]("descr")
    }
  }
  class Builder(val graph: TP.Graph, from: TP.Vertex, to: TP.Vertex) extends IBuilder {
    val edge: TP.Edge = from.add_edge(Def.label, to)
    def apply(v: String): this.type = {
      set_prop(_.name, v)
      this
    }
    def descr(v: String): this.type = {
      set_prop(_.descr, v)
      this
    }

    def build: FRT = HasElement(edge)
  }
}