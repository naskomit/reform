package sysmo.reform.shared.gremlin.tplight.steps.map

import sysmo.reform.shared.gremlin.tplight.{Element, Property, Traversal, Traverser}


class PropertyMapStep[E](keys: Seq[String]) extends MapStep[Element, Map[String, E]] {
  def map(traverser: Traverser[Element]): Map[String, E] = {
    val element = traverser.get
    val props: Iterator[Property[E]]  = if (keys.isEmpty) {
      element.properties[E]
    } else {
      keys.iterator.map(k => element.property[E](k))
    }

    props.map(p => (p.key, p.value)).collect {
      case (k, Some(v)) => (k, v)
    }.toMap

  }
}
