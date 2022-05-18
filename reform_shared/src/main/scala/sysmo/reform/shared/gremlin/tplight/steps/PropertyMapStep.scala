package sysmo.reform.shared.gremlin.tplight.steps

import sysmo.reform.shared.gremlin.tplight.{GraphTraversal, Traverser, Vertex, Edge, Element}

class PropertyMapStep[E](val traversal: GraphTraversal[_, _]) extends AbstractStep[Element, Map[String, E]] {
  override def process_next_start: Option[Traverser[Map[String, E]]] = ???

  def map(traverser: Traverser[_]): Map[String, E] = {
    val element = traverser.get match {
      case Some(e: Element) => e
    }
    element.properties[E].map(p => (p.key, p.value)).collect {
      case (k, Some(v)) => (k, v)
    }.toMap
  }
}
