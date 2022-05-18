package sysmo.reform.shared.gremlin.tplight

import sysmo.reform.shared.gremlin.tplight.steps.TraversalStep

trait Traverser[+T] {
  val t: Option[T] = None
  def get: Option[T] = t
  def bulk: Long = 1
  def set_bulk(count: Long): Unit = {}
  def dec_bulk(): Unit = set_bulk(bulk - 1)
  def path: Path = Path.Empty
}

object Traverser {
  class Empty[T] extends Traverser[T] {
    override def bulk: Long = 0
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
}

//trait TraverserGenerator {
//  def generate[S](start: S, start_step: TraversalStep[S], initial_bulk: Long)
//}

object TraverserGenerator {
  def generate[S](start: S, start_step: TraversalStep[_, S], initial_bulk: Long): B_O_Traverser[S] = {
    new B_O_Traverser[S](start, initial_bulk)
  }
}