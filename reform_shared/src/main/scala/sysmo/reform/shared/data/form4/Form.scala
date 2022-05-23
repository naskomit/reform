package sysmo.reform.shared.data.form4

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Vertex}
import sysmo.reform.shared.util.INamed
trait VertexObj {
  val vertex: Vertex
}
trait EdgeObj {
  val edge: Edge
}

trait ExressionNode extends VertexObj {
  def eval[T]: T
}

trait NamedElement extends VertexObj {
  def name: String = vertex.value[String]("name").get
  def descr: Option[String] = vertex.value[String]("descr")
  def show: Boolean = {
    val show_expr: Option[Vertex] = vertex.vertices(Direction.OUT, Seq("show_if"))
      .filter(v => v.label == "Expression").nextOption()
    show_expr.forall(_.asInstanceOf[ExressionNode].eval[Boolean])
  }
}

trait FieldEditor extends VertexObj {

}

class StringEditor(val vertex: Vertex) extends FieldEditor {

}


class SelectEditor(val vertex: Vertex) extends FieldEditor {

}

class FormGroup(val vertex: Vertex) extends VertexObj {
//  def children:
}