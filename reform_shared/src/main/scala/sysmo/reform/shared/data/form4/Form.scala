package sysmo.reform.shared.data.form4

import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.gremlin.tplight.gobject.{EdgeDef, EdgeObj, Property, PropertyDef, VertexDef}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalBuilder, GraphTraversalSource, Vertex}
import sysmo.reform.shared.util.INamed
import sysmo.reform.shared.{expr => E}

case class ElementPath(segments: Seq[String]) {
  def / (k: String): ElementPath = {
    val key_segments: Seq[String] = k.split("/")
    val new_segments: Seq[String] = key_segments.foldLeft(segments)((acc, segment) => segment match {
      case ".." => if (acc.nonEmpty) acc.dropRight(1) else acc
      case x => acc :+ x
    })
    ElementPath(new_segments)
  }
  override def toString: String = segments.mkString("/")
}
object ElementPath {
  val Empty = ElementPath(Seq())
}


//trait ExressionNode extends GO.VertexObj {
//  def eval[T]: T
//}

trait FormElementDef extends VertexDef {
  object Props extends PropertyDef {
    val name = GO.Property[String]("name")
    val descr = GO.Property[String]("descr")
    val show_expr = GO.Property[E.Expression]("show_expr", Some(E.Expression(true)))
  }
}

trait FormElement extends GO.VertexObj {
  override type ED <: FormElementDef
  def name: String = get(_.name).get
  def descr: String = get(_.descr).getOrElse(name)

  // TODO Very non-optimal, but need repeat for more efficient
  def parent: Option[FormElement] = g_this.in(HasElementDef.label).build.nextOption()
    .map(v => FormElement.from_vertex(v))

  def path: ElementPath = {
    parent match {
      case Some(p) => p.path / name
      case None => ElementPath(Seq(name))
    }
  }
  def show(ctx: HandlerContext): E.Result[Boolean] = {
    val show_expr: Expression = get(_.show_expr).get
    val res = E.as[Boolean](E.Expression.eval(show_expr, ctx))
    res
  }
}

object FormElement {
  trait Builder[+T <: FormElement] {
    protected val ed: FormElementDef
    protected val graph: Graph
    protected val name: String
    lazy val vertex: Vertex = graph.add_vertex(ed.label, ("name" -> name))

    def descr(v: String): this.type = {
      vertex.property(ed.Props.descr.name, v)
      this
    }
    def show(expr: E.Expression): this.type = {
      vertex.property(ed.Props.show_expr.name, expr)
      this
    }
    def build: T
  }

  def from_vertex(v: Vertex): FormElement = v.label match {
    case StringEditor.Def.label => StringEditor(v)
    case FloatEditor.Def.label => FloatEditor(v)
    case IntegerEditor.Def.label => IntegerEditor(v)
    case BooleanEditor.Def.label => BooleanEditor(v)
    case SelectEditor.Def.label => SelectEditor(v)
    case FormGroup.Def.label => FormGroup(v)
  }
}

trait FieldEditor extends FormElement {

}

object FieldEditor {
  trait Builder[+T <: FieldEditor] extends FormElement.Builder[T]
  type BuilderBase = Builder[FieldEditor]
  class BuilderSource(graph: Graph) {
    def char(name: String): StringEditor.Builder = new StringEditor.Builder(graph, name)
    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(graph, name)
    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(graph, name)
    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(graph, name)
    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(graph, name)
  }}

case class StringEditor(val vertex: Vertex) extends FieldEditor {
  override type ED = StringEditor.Def.type
  override val ed: ED = StringEditor.Def
}

object StringEditor {
  object Def extends FormElementDef {
    val label = "StringEditor"
  }
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[StringEditor] {
    override protected val ed: Def.type = Def
    override def build: StringEditor = {
      StringEditor(vertex)
    }
  }
}

case class FloatEditor(val vertex: Vertex) extends FieldEditor {
  override type ED = FloatEditor.Def.type
  override val ed: ED = FloatEditor.Def
}

object FloatEditor {
  object Def extends FormElementDef {
    val label: String = "FloatEditor"
  }
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[FloatEditor] {
    override protected val ed: Def.type = Def
    override def build: FloatEditor = {
      FloatEditor(vertex)
    }
  }
}

case class IntegerEditor(val vertex: Vertex) extends FieldEditor {
  override type ED = IntegerEditor.Def.type
  override val ed: ED = IntegerEditor.Def
}

object IntegerEditor {
  object Def extends FormElementDef {
    val label: String = "IntegerEditor"
  }

  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[IntegerEditor] {
    override protected val ed: Def.type = Def
    override def build: IntegerEditor = {
      IntegerEditor(vertex)
    }
  }
}

case class BooleanEditor(val vertex: Vertex) extends FieldEditor {
  override type ED = BooleanEditor.Def.type
  override val ed: ED = BooleanEditor.Def
}

object BooleanEditor {
  object Def extends FormElementDef {
    val label: String = "BooleanEditor"
  }

  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[BooleanEditor] {
    override protected val ed: Def.type = Def
    override def build: BooleanEditor = {
      BooleanEditor(vertex)
    }
  }
}

case class SelectEditor(val vertex: Vertex) extends FieldEditor {
  override type ED = SelectEditor.Def.type
  override val ed: ED = SelectEditor.Def
}

object SelectEditor {
  object Def extends FormElementDef {
    val label: String = "SelectEditor"
  }

  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[SelectEditor] {
    override protected val ed: Def.type = Def
    override def build: SelectEditor = {
      new SelectEditor(vertex)
    }
  }

}

case class FormGroup(val vertex: Vertex) extends FormElement {
  override type ED = FormGroup.Def.type
  override val ed: ED = FormGroup.Def

  def elements: Seq[FormElement] = {
    g_this.outE(HasElementDef.label).build
      .toSeq.sortBy(e => HasElement(e).get(_.seq_num).getOrElse(0))
      .map(e => FormElement.from_vertex(e.in_vertex))
  }
}

case class HasElement(edge: Edge) extends EdgeObj {
  override type ED = HasElementDef.type
  override val ed: ED = HasElementDef
}

object HasElementDef extends EdgeDef {
  override val label: String = "has_element"
  object Props extends PropertyDef {
    val seq_num = Property[Int]("seq_num")
  }
}

object FormGroup {
  object Def extends FormElementDef {
    val label: String = "FormGroup"
  }
  type BuilderBase = FormElement.Builder[FormGroup]
//  val rel_element = "has_element"
  // Rel properties
//  val seq_num = "seq_num"

  class Builder(val graph: Graph, val name: String) extends FormElement.Builder[FormGroup] {
    override protected val ed: Def.type = Def
    protected def add_element(element: FormElement): this.type = {
      val new_seq_num = vertex.edges(Direction.OUT, Seq(HasElementDef.label))
        .map(e => HasElement(e))
        .map(e => e.get(_.seq_num).getOrElse(-1)).toSeq.sorted.lastOption.getOrElse(-1) + 1
      vertex.add_edge(HasElementDef.label, element.vertex, (HasElementDef.Props.seq_num.name -> new_seq_num))
      this
    }

    def group(name: String, f: Builder => BuilderBase): this.type = {
      add_element(
        f(new FormGroup.Builder(graph, name)).build
      )
    }

    def field(f: FieldEditor.BuilderSource => FieldEditor.BuilderBase): this.type = {
      add_element(
        f(new FieldEditor.BuilderSource(graph)).build
      )
    }

    override def build: FormGroup = {
      new FormGroup(vertex)
    }
  }

  def builder(graph: Graph, name: String): Builder = new Builder(graph, name)

}