package sysmo.reform.shared.data.form4

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalBuilder, GraphTraversalSource, Vertex}
import sysmo.reform.shared.util.INamed
import sysmo.reform.shared.expr.{Expression => E}

case class ElementPath(segments: Seq[String]) {
  def / (k: String): ElementPath = ElementPath(segments :+ k)
  override def toString: String = segments.mkString("/")
}
object ElementPath {
  val Empty = ElementPath(Seq())
}


trait ExressionNode extends VertexObj {
  def eval[T]: T
}

trait FormElement extends VertexObj {
  def name: String = vertex.value[String]("name").get
  def descr: String = vertex.value[String]("descr").getOrElse(name)
  // TODO Very non-optimal, but need repeat for more efficient
  def parent: Option[FormElement] = g_this.in(FormGroup.rel_element).build.nextOption()
    .map {v => new FormElement {val vertex = v}}
  def path: ElementPath = {
    parent match {
      case Some(p) => p.path / name
      case None => ElementPath(Seq(name))
    }
  }
  def show: Boolean = {
    val show_expr: Option[Vertex] = vertex.vertices(Direction.OUT, Seq("show_if"))
      .filter(v => v.label == "Expression").nextOption()
    show_expr.forall(_.asInstanceOf[ExressionNode].eval[Boolean])
  }
}

object FormElement {
  trait Builder[+T] {
    protected val graph: Graph
    protected val name: String
    protected val _label: String
    lazy val vertex: Vertex = graph.add_vertex(_label, ("name" -> name))
    def descr(v: String): this.type = {
      vertex.property("descr", v)
      this
    }
    def build: T
  }

  def from_vertex(v: Vertex): FormElement = v.label match {
    case StringEditor.label => StringEditor(v)
    case FloatEditor.label => FloatEditor(v)
    case IntegerEditor.label => IntegerEditor(v)
    case BooleanEditor.label => BooleanEditor(v)
    case SelectEditor.label => SelectEditor(v)
    case FormGroup.label => FormGroup(v)
  }
}

sealed trait FieldEditor extends FormElement {

}

object FieldEditor {
  trait Builder[+T] extends FormElement.Builder[T]
  type BuilderBase = Builder[FieldEditor]
  class BuilderSource(graph: Graph) {
    def char(name: String): StringEditor.Builder = new StringEditor.Builder(graph, name)
    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(graph, name)
    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(graph, name)
    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(graph, name)
    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(graph, name)
  }}

case class StringEditor(val vertex: Vertex) extends FieldEditor {
}
object StringEditor {
  val label: String = "StringEditor"
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[StringEditor] {
    override protected val _label: String = label
    override def build: StringEditor = {
      StringEditor(vertex)
    }
  }
}

case class FloatEditor(val vertex: Vertex) extends FieldEditor {
}

object FloatEditor {
  val label: String = "FloatEditor"
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[FloatEditor] {
    override protected val _label: String = label
    override def build: FloatEditor = {
      FloatEditor(vertex)
    }
  }
}

case class IntegerEditor(val vertex: Vertex) extends FieldEditor {
}

object IntegerEditor {
  val label: String = "IntegerEditor"
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[IntegerEditor] {
    override protected val _label: String = label
    override def build: IntegerEditor = {
      IntegerEditor(vertex)
    }
  }
}

case class BooleanEditor(val vertex: Vertex) extends FieldEditor {
}

object BooleanEditor {
  val label: String = "BooleanEditor"
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[BooleanEditor] {
    override protected val _label: String = label
    override def build: BooleanEditor = {
      BooleanEditor(vertex)
    }
  }
}

case class SelectEditor(val vertex: Vertex) extends FieldEditor {
}

object SelectEditor {
  val label: String = "SelectEditor"
  class Builder(val graph: Graph, val name: String) extends FieldEditor.Builder[SelectEditor] {
    override protected val _label: String = label
    override def build: SelectEditor = {
      new SelectEditor(vertex)
    }
  }

}

case class FormGroup(val vertex: Vertex) extends FormElement {
  def elements: Seq[FormElement] = {
    g_this.out("has_element").build
      .toSeq.sortBy(x => x.value[Int](FormGroup.seq_num).getOrElse(0))
      .map(FormElement.from_vertex)
  }
}

object FormGroup {
  type BuilderBase = FormElement.Builder[FormGroup]
  val label: String = "FormGroup"
  val rel_element = "has_element"
  // Rel properties
  val seq_num = "seq_num"

  class Builder(val graph: Graph, val name: String) extends FormElement.Builder[FormGroup] {
    override protected val _label: String = label

    protected def add_element(element: FormElement): this.type = {
      val new_seq_num = vertex.edges(Direction.OUT, Seq(rel_element))
        .map(e => e.value[Int](seq_num).getOrElse(-1)).toSeq.sorted.lastOption.getOrElse(-1) + 1
      vertex.add_edge("has_element", element.vertex, (seq_num -> new_seq_num))
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