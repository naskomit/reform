package sysmo.reform.shared.gremlin.tplight

import sysmo.reform.shared.gremlin.bytecode.Symbols
import sysmo.reform.shared.gremlin.tplight.steps.filter.LambdaFilterStep
import sysmo.reform.shared.gremlin.tplight.steps.map.{EdgeVertexStep, LambdaFlatMapStep, LambdaMapStep, PropertyMapStep, VertexStep}
import sysmo.reform.shared.gremlin.tplight.steps.side_effect.LambdaSideEffectStep
import sysmo.reform.shared.gremlin.tplight.{steps => ST}

case class Neighbours[A](prev: Map[A, A], next: Map[A, A])
case class Path(objects: Seq[Any], labels: Seq[Set[String]]) {

}

object Path {
  object Empty extends Path(Seq(), Seq(Set()))
}

trait Traversal[S, +E] extends Iterator[E] {
  val graph: Graph
  val steps: Seq[TraversalStep[_, _]]
  val bytecode: Bytecode
  protected val neighbours: Neighbours[TraversalStep[_, _]]
  def next_step(s: TraversalStep[_, _]): Option[TraversalStep[_, _]] = {
    neighbours.next.get(s)
  }

  def prev_step(s: TraversalStep[_, _]): Option[TraversalStep[_, _]] = {
    neighbours.prev.get(s)
  }
}

class DefaultTraversal[S <: Element, E](
  val graph: Graph, val steps: Seq[TraversalStep[_, _]],
  val bytecode: Bytecode, val neighbours: Neighbours[TraversalStep[_, _]])
extends Traversal[S, E] {

  private var last_traverser: Traverser[E] = Traverser.Empty[E]
  private val final_end_step: TraversalStep[_, E] =
    if (steps.isEmpty) TraversalStep.Empty[Nothing, E] else steps.last.asInstanceOf[TraversalStep[_, E]]

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
}

class GraphTraversalBuilder[S <: Element, E](val graph: Graph) {
  protected var steps: Seq[TraversalStep[_, _]] = Seq()
  protected var bytecode: Bytecode = Bytecode.Empty
  var neighbours: Neighbours[TraversalStep[_, _]] = Neighbours(Map(), Map())

  def add_step[E2](s: TraversalStep[E, E2], bc_xform: Bytecode => Bytecode): GraphTraversalBuilder[S, E2] = {
    bytecode = bc_xform(bytecode)
    neighbours = if (steps.nonEmpty) {
      neighbours.copy(
        prev = neighbours.prev + (s -> steps.last),
        next = neighbours.next + (steps.last -> s)
      )
    } else neighbours
    steps = steps :+ s
//    new GraphTraversal[S, E2](graph, new_bytecode, new_steps, new_neighbours)
    this.asInstanceOf[GraphTraversalBuilder[S, E2]]
  }



  /** Graph traversal steps */

  def valueMap[E2](keys: String*): GraphTraversalBuilder[S, Map[String, E2]] = {
    val step =
      (new ST.map.PropertyMapStep[E2](keys))
    this.add_step[Map[String, E2]](
      step.asInstanceOf[TraversalStep[E, Map[String, E2]]],
      bc => bc.add_step(Symbols.valueMap, keys)
    )
  }

  protected def vertex_vertex_step(dir: Direction, edge_labels: Seq[String]): GraphTraversalBuilder[S, Vertex] = {
    import VertexStep._
    val step = new VertexStep[Vertex](dir, edge_labels)
    val symbol = dir match {
      case Direction.IN => Symbols.in
      case Direction.OUT => Symbols.out
      case Direction.BOTH => Symbols.both
    }
    val traversal = this.asInstanceOf[GraphTraversalBuilder[S, Vertex]]
    traversal.add_step[Vertex](step,
      bc => bc.add_step(symbol, edge_labels)
    )
  }

  def in(edge_labels: String*): GraphTraversalBuilder[S, Vertex] = vertex_vertex_step(Direction.IN, edge_labels)
  def out(edge_labels: String*): GraphTraversalBuilder[S, Vertex] = vertex_vertex_step(Direction.OUT, edge_labels)
  def both(edge_labels: String*): GraphTraversalBuilder[S, Vertex] = vertex_vertex_step(Direction.BOTH, edge_labels)

  protected def vertex_edge_step(dir: Direction, edge_labels: Seq[String]): GraphTraversalBuilder[S, Edge] = {
    import VertexStep._
    val step = new VertexStep[Edge](dir, edge_labels)
    val symbol = dir match {
      case Direction.IN => Symbols.inE
      case Direction.OUT => Symbols.outE
      case Direction.BOTH => Symbols.bothE
    }
    val traversal = this.asInstanceOf[GraphTraversalBuilder[S, Vertex]]
    traversal.add_step[Edge](step,
      bc => bc.add_step(symbol, edge_labels)
    )
  }

  def inE(edge_labels: String*): GraphTraversalBuilder[S, Edge] = vertex_edge_step(Direction.IN, edge_labels)
  def outE(edge_labels: String*): GraphTraversalBuilder[S, Edge] = vertex_edge_step(Direction.OUT, edge_labels)
  def bothE(edge_labels: String*): GraphTraversalBuilder[S, Edge] = vertex_edge_step(Direction.BOTH, edge_labels)

  protected def edge_vertex_step(dir: Direction): GraphTraversalBuilder[S, Vertex] = {
    val step = new EdgeVertexStep(dir)
    val symbol = dir match {
      case Direction.IN => Symbols.inV
      case Direction.OUT => Symbols.outV
      case Direction.BOTH => Symbols.bothV
    }
    val traversal = this.asInstanceOf[GraphTraversalBuilder[S, Edge]]
    traversal.add_step[Vertex](step,
      bc => bc.add_step(symbol)
    )
  }

  def inV(): GraphTraversalBuilder[S, Vertex] = edge_vertex_step(Direction.IN)
  def outV(): GraphTraversalBuilder[S, Vertex] = edge_vertex_step(Direction.OUT)
  def bothV(): GraphTraversalBuilder[S, Vertex] = edge_vertex_step(Direction.BOTH)

  def map[E2](f: Traverser[E] => E2): GraphTraversalBuilder[S, E2] = {
    val step = new LambdaMapStep[E, E2](f)
    this.add_step[E2](step,
      bc => bc.add_step(Symbols.map, f)
    )
  }

  def flatMap[E2](f: Traverser[E] => Iterator[E2]): GraphTraversalBuilder[S, E2] = {
    val step = new LambdaFlatMapStep[E, E2](f)
    this.add_step[E2](step,
      bc => bc.add_step(Symbols.flatMap, f)
    )
  }

  def filter(f: Traverser[E] => Boolean): GraphTraversalBuilder[S, E] = {
    val step = new LambdaFilterStep[E](f)
    this.add_step[E](step,
      bc => bc.add_step(Symbols.filter, f)
    )
  }

  /** Extended operations */
  def sideEffect(f: Traverser[E] => Unit): GraphTraversalBuilder[S, E] = {
    val step = new LambdaSideEffectStep[E](f)
    this.add_step[E](step,
      bc => bc.add_step(Symbols.sideEffect, f)
    )
  }

  def build: Traversal[S, E] = {
    val traversal = new DefaultTraversal[S, E](
      graph, steps, bytecode, neighbours
    )
    for (step <- traversal.steps) {
      step.traversal = traversal
    }
    traversal
  }

}
