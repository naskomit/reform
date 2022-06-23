package sysmo.reform.shared.form.build

import sysmo.reform.shared.form.build.FieldGroup._cmp
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.{tplight => TP}

case class FieldGroup(vertex: TP.Vertex) extends AbstractGroup {
  override type ED = FieldGroup.Def.type
  override val ed = FieldGroup.Def
//  lazy val child_map: Map[String, FormElement] = vertex.edges(TP.Direction.OUT, Seq(HasElement.Def.label)).map{ e =>
//    val field = FormElement.from_vertex(e.in_vertex).get
//    (HasElement(e).get(_.name).get, field)
//  }.toMap
  def symbol: String = get(_.symbol).get
  def layout: Option[String] = get(_.layout)

  def field(name: String): Option[FormElement] =
    vertex.edges(TP.Direction.OUT, Seq(HasElement.Def.label)).find{ e =>
    HasElement(e).get(_.name).contains(name)
  }.flatMap(e => FormElement.from_vertex(e.in_vertex))

  def field_relations: Seq[HasElement] = {
    vertex.edges(TP.Direction.OUT, Seq(HasElement.Def.label))
      .map(e => HasElement(e)).toSeq.sortBy(_.get(_.seq_num))
  }

  def field_rel(name: String): Option[HasElement] =
    vertex.edges(TP.Direction.OUT, Seq(HasElement.Def.label)).find{ e =>
      HasElement(e).get(_.name).contains(name)
    }.map(e => HasElement(e))

}

object FieldGroup extends FormElementCompanion[FieldGroup] {
  object Def extends IDef {
    val label = "FieldGroup"

    object props extends Props {
      val symbol: GO.Property[String] = GO.Property[String]("symbol")
      val layout: GO.Property[String] = GO.Property[String]("layout")
    }
  }
  class Builder(val graph: TP.Graph, symbol: String) extends IBuilder {
    set_prop(_.symbol, symbol)
    def field(f_rel: HasElement.Builder => HasElement.Builder,
              f_child: FieldBuilderSource => FormElementCompanion[_]#IBuilder): this.type = {
      val child_builder = f_child(new FieldBuilderSource(graph))
      f_rel(new HasElement.Builder(graph, vertex, child_builder.vertex))
      this
    }

    def group(f_rel: HasElement.Builder => HasElement.Builder,
              child_builder: FieldGroup.Builder): this.type = {
      f_rel(new HasElement.Builder(graph, vertex, child_builder.vertex))
      this
    }

    def group(f_rel: HasElement.Builder => HasElement.Builder,
              child_builder: GroupUnion.Builder): this.type = {
      f_rel(new HasElement.Builder(graph, vertex, child_builder.vertex))
      this
    }

    def array(f_rel: HasElement.Builder => HasElement.Builder,
              child_builder: FieldGroup.Builder): this.type = {
      val array_builder = GroupArray.builder(graph)
      f_rel(new HasElement.Builder(graph, vertex, array_builder.vertex))
      new HasPrototype.Builder(graph, array_builder.vertex, child_builder.vertex)
      this
    }

    def array(f_rel: HasElement.Builder => HasElement.Builder,
              child_builder: GroupUnion.Builder): this.type = {
      val array_builder = GroupArray.builder(graph)
      f_rel(new HasElement.Builder(graph, vertex, array_builder.vertex))
      new HasPrototype.Builder(graph, array_builder.vertex, child_builder.vertex)
      this
    }

    def union (symbol: String, f_union: GroupUnion.Builder => GroupUnion.Builder): GroupUnion.Builder = {
      val union_builder = GroupUnion.builder(graph, symbol)
      f_union(union_builder | this)
    }

    def layout(v: String): this.type = {
      set_prop(_.layout, v)
      this
    }

    def build: FET = new FieldGroup(vertex)
  }

  class FieldBuilderSource(graph: TP.Graph) {
    def char: StringField.Builder = new StringField.Builder(graph)
    //    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(graph, parent, name)
    //    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(graph, parent, name)
    //    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(graph, parent, name)
    //    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(graph, parent, name)
  }

  implicit val _cmp: FormElementCompanion[FieldGroup] = this
  def builder(graph: TP.Graph, symbol: String): Builder = new Builder(graph, symbol)
}

case class HasElement(edge: TP.Edge) extends FormRelation {
  override type ED = HasElement.Def.type
  override val ed = HasElement.Def
  def name: String = get(_.name).get
  def descr: String = get(_.descr).getOrElse(name)
  def child_field: FormElement = FormElement.from_vertex(edge.in_vertex).get
}

object HasElement extends FormRelationCompanion[HasElement] {
  object Def extends IDef {
    val label: String = "HasElement"

    object props extends Props {
      val name: GO.Property[String] = GO.Property[String]("name")
      val descr: GO.Property[String] = GO.Property[String]("descr")
      val seq_num: GO.Property[Int] = GO.Property[Int]("seq_num")
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

case class HasPrototype(edge: TP.Edge) extends FormRelation {
  override type ED = HasPrototype.Def.type
  override val ed = HasPrototype.Def
  def prototype: FormElement = FormElement.from_vertex(edge.in_vertex).get
}

object HasPrototype extends FormRelationCompanion[HasPrototype] {
  object Def extends IDef {
    val label: String = "HasPrototype"

    object props extends Props {
      val descr: GO.Property[String] = GO.Property[String]("descr")
    }
  }

  class Builder(val graph: TP.Graph, from: TP.Vertex, to: TP.Vertex) extends IBuilder {
    val edge: TP.Edge = from.add_edge(Def.label, to)

    def descr(v: String): this.type = {
      set_prop(_.descr, v)
      this
    }

    def build: FRT = HasPrototype(edge)
  }
}