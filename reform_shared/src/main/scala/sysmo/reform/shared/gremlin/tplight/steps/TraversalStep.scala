package sysmo.reform.shared.gremlin.tplight.steps


import sysmo.reform.shared.gremlin.tplight.{GraphTraversal, Traverser}

import scala.annotation.tailrec

trait TraversalStep[S, E] extends Iterator[Traverser[E]] {
  val traversal: GraphTraversal[_, _]
  def next_step: Option[TraversalStep[E ,_]] = traversal.neighbours.next.get(this).map(_.asInstanceOf[TraversalStep[E ,_]])
  def prev_step: Option[TraversalStep[_, S]] = traversal.neighbours.prev.get(this).map(_.asInstanceOf[TraversalStep[_ ,S]])
}

trait AbstractStep[S, E] extends TraversalStep[S, E] {
  var next_end: Option[Traverser[E]] = None
  def process_next_start: Option[Traverser[E]]

  // TODO
  private def prepare_traversal_for_next_step(t: Traverser[E]): Traverser[E] =
    t

  @tailrec
  private def produce_traverser: Option[Traverser[E]] = process_next_start match {
    case Some(trav) => trav.get match {
      case Some(_) if trav.bulk != 0 => Some(trav)
      case None => produce_traverser
    }
    case None => None
  }

  override def hasNext: Boolean = {
    next_end match {
      case Some(_) => true
      case None =>  {
        produce_traverser match {
          case Some(trav) => {
            next_end = Some(trav)
            true
          }
          case None => false
        }

      }
    }
  }

  override def next(): Traverser[E] = {
    next_end match {
      case Some(trav) => {
        val t1 = prepare_traversal_for_next_step(trav)
        next_end = None
        t1
      }
      case None => {
        val t1 = prepare_traversal_for_next_step(produce_traverser.get)
        t1
      }
    }
  }
}

object TraversalStep {
  class Empty[S, E] extends TraversalStep[S, E] {
    val traversal = null
    override def hasNext: Boolean = false
    override def next(): Traverser[E] =
      throw new IllegalAccessException("This code should not be reachable!")
  }
  object Empty {
    def apply[S, E]: Empty[S, E] = new Empty[S, E]
  }
}