package sysmo.reform.shared.gremlin.tplight.steps

import sysmo.reform.shared.gremlin.tplight.{AbstractStep, GraphTraversalBuilder, Traversal, Traverser}

package object side_effect {
  trait SideEffectStep[S] extends AbstractStep[S, S] {
    def side_effect(traverser: Traverser[S]): Unit

    override def process_next_start: Option[Traverser[S]] = {
      if (starts.hasNext) {
        val traverser = starts.next()
        side_effect(traverser)
        Some(traverser)
      } else {
        None
      }
    }
  }

  class LambdaSideEffectStep[S](f: Traverser[S] => Unit)
  extends SideEffectStep[S] {
    @inline
    final override def side_effect(traverser: Traverser[S]): Unit = f(traverser)
  }
}
