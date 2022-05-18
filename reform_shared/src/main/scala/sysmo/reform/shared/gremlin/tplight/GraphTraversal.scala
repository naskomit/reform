package sysmo.reform.shared.gremlin.tplight

import sysmo.reform.shared.gremlin.bytecode
import sysmo.reform.shared.gremlin.tplight.steps.TraversalStep

import scala.annotation.tailrec

//type NeighbourMap = Map[TraversalStep[_], TraversalStep[_]]
//val prev_steps: NeighbourMap
//  val next_steps: NeighbourMap

case class Neighbours[A](prev: Map[A, A], next: Map[A, A])

case class Path(objects: Seq[Any], labels: Seq[Set[String]]) {

}

object Path {
  object Empty extends Path(Seq(), Seq(Set()))
}

trait Traversal[S, +E] extends Iterator[E] {
  val neighbours: Neighbours[TraversalStep[_, _]]

}


class GraphTraversal[S, E](val graph: Graph, val bytecode: Bytecode, val steps: Seq[TraversalStep[_, _]],
                         val neighbours: Neighbours[TraversalStep[_, _]]) extends Traversal[S, E] {

  private var last_traverser: Traverser[E] = Traverser.Empty[E]
  private val final_end_step: TraversalStep[_, E] =
    if (steps.nonEmpty) steps.last.asInstanceOf[TraversalStep[_, E]] else TraversalStep.Empty[Nothing, E]

  def add_step[S2, E2](s: TraversalStep[S2, E2], bc_xform: Bytecode => Bytecode): GraphTraversal[S2, E2] = {
    val new_bytecode = bc_xform(bytecode)
    val new_steps = steps :+ s
    val new_neighbours = if (steps.nonEmpty) {
      neighbours.copy(
        prev = neighbours.prev + (s -> steps.last),
        next = neighbours.next + (steps.last -> s)
      )
    } else neighbours
    new GraphTraversal[S2, E2](graph, new_bytecode, new_steps, new_neighbours)
  }

  override def hasNext: Boolean =
    final_end_step.hasNext

  override def next(): E = {
    if (last_traverser.bulk == 0) {
      last_traverser = final_end_step.next()
    }
    last_traverser.dec_bulk()
    last_traverser.get match {
      case Some(v) => v
      case None => throw new NoSuchElementException()
    }
  }

  /** Graph traversal steps */

//  def valueMap(keys: String*) =




}

object GraphTraversal {
  class Empty(graph: Graph) extends GraphTraversal[Nothing, Nothing](graph, Bytecode.Empty, Seq(), Neighbours(Map(), Map()))
}
