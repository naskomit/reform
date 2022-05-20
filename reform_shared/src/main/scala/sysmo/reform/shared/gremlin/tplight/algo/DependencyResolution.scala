package sysmo.reform.shared.gremlin.tplight.algo

import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalSource, Vertex}

import scala.annotation.tailrec
import scala.collection.mutable

class DependencyResolution(graph: Graph) {
  val g = graph.traversal()

  def find_affected(start: Set[Vertex], edge_predicate: Edge => Boolean = {x => true}): Set[Vertex] = {
    val affected = mutable.HashSet[Vertex]()
    for (v <- start) {
      _find_affected(Seq(v.vertices(Direction.OUT, Seq())), affected)
    }
    affected.toSet
  }

  @tailrec
  private def _find_affected(remaining_out: Seq[Iterator[Vertex]], acc: mutable.Set[Vertex]): Unit = {
    if (remaining_out.nonEmpty) {
      if (remaining_out.last.hasNext) {
        val current = remaining_out.last.next()
        if (acc.contains(current)) {
          _find_affected(remaining_out, acc)
        } else {
          acc.add(current)
          _find_affected(remaining_out :+ current.vertices(Direction.OUT, Seq()), acc)
        }
      } else {
        _find_affected(remaining_out.dropRight(1), acc)
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
