package sysmo.reform.shared.gremlin.tplight.steps

import sysmo.reform.shared.gremlin.tplight.{Edge, Element, Graph, GraphTraversal, Traverser, TraverserGenerator, Vertex}

import scala.annotation.tailrec

class GraphStep[S, E <: Element](val traversal: GraphTraversal[_, _], ids: Seq[Any])
                             (implicit iter_supplier: (Graph, Seq[Any]) => Iterator[E])
  extends AbstractStep[S, E] {
  val iter: Iterator[E] = iter_supplier(traversal.graph, ids)
  var done: Boolean = false

  override def process_next_start: Option[Traverser[E]] = _proc_next

//  @tailrec
  private def _proc_next: Option[Traverser[E]] = {
    if (iter.hasNext) {
      Some(TraverserGenerator.generate[E](iter.next, this, 1))
    } else {
      done = true
      None
    }
  }
}

object GraphStep {
  implicit val vertex_iter_supplier: (Graph, Seq[Any]) => Iterator[Vertex] =
    (g, ids) => g.vertices(ids: _*)

  implicit val edge_iter_supplier: (Graph, Seq[Any]) => Iterator[Edge] =
    (g, ids) => g.edges(ids: _*)
}
