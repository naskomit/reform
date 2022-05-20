package sysmo.reform.shared.gremlin.tplight

import scala.annotation.tailrec

trait TraversalStep[S, E] extends Iterator[Traverser[E]] {
  var traversal: Traversal[_, _] = null
  def next_step: Option[TraversalStep[E ,_]] = traversal.next_step(this).map(_.asInstanceOf[TraversalStep[E ,_]])
  def prev_step: Option[TraversalStep[_, S]] = traversal.prev_step(this).map(_.asInstanceOf[TraversalStep[_ ,S]])
}

trait AbstractStep[S, E] extends TraversalStep[S, E] {
  protected val starts: ExpandableStepIterator[S] = new ExpandableStepIterator[S](this)
  protected var next_end: Option[Traverser[E]] = None
  def process_next_start: Option[Traverser[E]]

  // TODO
  private def prepare_traversal_for_next_step(t: Traverser[E]): Traverser[E] =
    t

  @tailrec
  private def produce_traverser: Option[Traverser[E]] = process_next_start match {
    case Some(trav) if trav.bulk != 0 => Some(trav)
    case None => None
    case _ => produce_traverser
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
    override def hasNext: Boolean = false
    override def next(): Traverser[E] =
      throw new NoSuchElementException
  }
  object Empty {
    def apply[S, E]: Empty[S, E] = new Empty[S, E]
  }
}