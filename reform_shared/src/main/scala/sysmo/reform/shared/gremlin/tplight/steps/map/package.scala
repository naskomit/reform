package sysmo.reform.shared.gremlin.tplight.steps

import sysmo.reform.shared.gremlin.tplight.Traversal

package object map {
  import sysmo.reform.shared.gremlin.tplight.{AbstractStep, Traverser, GraphTraversalBuilder}

  import scala.annotation.tailrec

  trait MapStep[S, E] extends AbstractStep[S, E] {
    def map(traverser: Traverser[S]): E
    final override def process_next_start: Option[Traverser[E]] = {
      if (starts.hasNext) {
        val traverser = starts.next()
        Some(traverser.split(map(traverser), this))
      } else None
    }
  }

  trait FlatMapStep[S, E] extends AbstractStep[S, E] {
    var iter: Iterator[E] = Iterator.empty[E]
    var head: Option[Traverser[S]] = None
    def flat_map(traverser: Traverser[S]): Iterator[E]
    final override def process_next_start: Option[Traverser[E]] = proc_next

    @tailrec
    private def proc_next: Option[Traverser[E]] = {
      if (iter.hasNext) {
        Some(head.get.split(iter.next(), this))
      } else if (starts.hasNext) {
        head = Some(starts.next())
        iter = flat_map(head.get)
        proc_next
      } else None

    }
  }

  class LambdaMapStep[S, E](f: Traverser[S] => E)
    extends MapStep[S, E] {
    @inline
    final override def map(traverser: Traverser[S]): E = f(traverser)
  }

  class LambdaFlatMapStep[S, E](f: Traverser[S] => Iterator[E])
    extends FlatMapStep[S, E] {
    @inline
    final override def flat_map(traverser: Traverser[S]): Iterator[E] = f(traverser)
  }
}
