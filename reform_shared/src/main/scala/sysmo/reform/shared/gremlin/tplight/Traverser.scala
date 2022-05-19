package sysmo.reform.shared.gremlin.tplight

import scala.collection.mutable

trait Traverser[T] {
  val t: Option[T] = None
  def get: Option[T] = t
  def bulk: Long = 1
  def set_bulk(count: Long): Unit = {}
  def dec_bulk(): Unit = set_bulk(bulk - 1)
  def path: Path = Path.Empty
  def split[E](value: E, step: TraversalStep[T, E]): Traverser[E]
}

object Traverser {
  class Empty[T] extends Traverser[T] {
    override def bulk: Long = 0
    override def split[E](value: E, step: TraversalStep[T, E]): Traverser[E] = Empty[E]
  }
  object Empty {
    def apply[T]: Empty[T] = new Empty[T]
  }
}

class B_O_Traverser[T](start: T, initial_bulk: Long) extends Traverser[T] {
  override val t = Some(start)
  private var _bulk = initial_bulk
  override def bulk: Long = _bulk
  override def set_bulk(count: Long): Unit = {
    _bulk = count
  }
  override def split[E](value: E, step: TraversalStep[T, E]): Traverser[E] = {
    new B_O_Traverser[E](value, 1)
  }
}

//trait TraverserGenerator {
//  def generate[S](start: S, start_step: TraversalStep[S], initial_bulk: Long)
//}

object TraverserGenerator {
  def generate[S](start: S, start_step: TraversalStep[_, S], initial_bulk: Long): B_O_Traverser[S] = {
    new B_O_Traverser[S](start, initial_bulk)
  }
}

class ExpandableStepIterator[S] (host_step: TraversalStep[S, _]) extends Iterator[Traverser[S]] {
  private val traversers: mutable.Set[Traverser[S]] = mutable.HashSet[Traverser[S]]()

  def push(t: Traverser[S]): Unit = {
    traversers.add(t)
  }

  def pop: Traverser[S] = {
    val result = traversers.head
    traversers.remove(result)
    result
  }

  override def hasNext: Boolean = {
    traversers.nonEmpty || host_step.prev_step.exists(_.hasNext)
  }

  override def next(): Traverser[S] = {
    if (traversers.nonEmpty) {
      pop
    } else if (host_step.prev_step.exists(_.hasNext)) {
      host_step.prev_step.get.next()
    } else {
      throw new IllegalStateException("Must check with hasNext!")
    }
  }
}
