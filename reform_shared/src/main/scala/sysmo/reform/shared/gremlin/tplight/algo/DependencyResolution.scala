package sysmo.reform.shared.gremlin.tplight.algo

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalSource, Vertex}

import scala.annotation.tailrec
import scala.collection.mutable

class DependencyResolution(graph: Graph) {
  val g = graph.traversal()
  type Vertex2Vertex = Vertex => Iterator[Vertex]

  def find_affected(start: Set[Vertex], edge_predicate: Edge => Boolean = {x => true}): Set[Vertex] = {
    val step: Vertex2Vertex = {v =>
      v.edges(Direction.OUT, Seq()).filter(edge_predicate).map(x => x.in_vertex)
    }
    val affected = mutable.HashSet[Vertex]()
    for (v <- start) {
      _find_affected(step, Seq(step(v)), affected)
    }
    affected.toSet
  }

  @tailrec
  private def _find_affected(step: Vertex2Vertex, remaining_out: Seq[Iterator[Vertex]], acc: mutable.Set[Vertex]): Unit = {
    if (remaining_out.nonEmpty) {
      if (remaining_out.last.hasNext) {
        val current = remaining_out.last.next()
        if (acc.contains(current)) {
          _find_affected(step, remaining_out, acc)
        } else {
          acc.add(current)
          _find_affected(step, remaining_out :+ step(current), acc)
        }
      } else {
        _find_affected(step, remaining_out.dropRight(1), acc)
      }
    } else {

    }
  }

  @tailrec
  private def _resolve(current: Set[Vertex], unresolved: Set[Vertex], edge_predicate: Edge => Boolean, resolved: Seq[Set[Vertex]]): Seq[Set[Vertex]] = {
    val next_level: Set[Vertex] = g.V(current.map(_.id).toSeq: _*).outE().filter(
      t => edge_predicate(t.get)
    ).inV().filter(
      t =>
        t.get.vertices(Direction.IN, Seq()).toSet.intersect(unresolved).isEmpty
    ).build.toSet

    if (next_level.nonEmpty) {
      _resolve(next_level, unresolved.diff(next_level), edge_predicate, resolved :+ next_level)
    } else {
      resolved
    }
  }

  def resolution_seq(start: Set[Vertex], edge_predicate: Edge => Boolean = {e => true}): Seq[Set[Vertex]] = {
    val affected = find_affected(start, edge_predicate)
    _resolve(start, affected, edge_predicate, Seq())
  }
}
