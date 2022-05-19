package sysmo.reform.shared.gremlin.tplight.steps

import sysmo.reform.shared.gremlin.tplight.{AbstractStep, GraphTraversalBuilder, Traversal, Traverser}

import scala.annotation.tailrec

package object filter {

  trait FilterStep[S] extends AbstractStep[S, S] {
    def filter(traverser: Traverser[S]): Boolean
    @inline
    final override def process_next_start: Option[Traverser[S]] = proc_next

    @tailrec
    private def proc_next: Option[Traverser[S]] = {
      if (starts.hasNext) {
        val traverser = starts.next()
        if (filter(traverser)) {
          Some(traverser)
        } else {
          proc_next
        }
      } else {
        None
      }
    }
  }

  class LambdaFilterStep[S](f: Traverser[S] => Boolean)
    extends FilterStep[S] {
    @inline
    final override def filter(traverser: Traverser[S]): Boolean = f(traverser)
  }

}
