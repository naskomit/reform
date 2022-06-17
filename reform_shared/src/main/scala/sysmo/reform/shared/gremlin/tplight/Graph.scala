package sysmo.reform.shared.gremlin.tplight

import scala.language.implicitConversions

//trait PropId

object PropId {
  val label: String = "LABEL"
  val id: String = "ID"
  val key: String = "KEY"
  val value: String = "VALUE"
//  case object label extends PropId {
//    override def toString: String = "LABEL"
//  }
//  case object id extends PropId {
//    override def toString: String = "ID"
//  }
//  case object key extends PropId {
//    override def toString: String = "KEY"
//  }
//
//  case object value extends PropId {
//    override def toString: String = "VALUE"
//  }
//  case class StringId(v: String) extends PropId
//
//  implicit def Str2PropId(x: Tuple2[String, Any]): Tuple2[PropId, Any] =
//    PropId.StringId(x._1) -> x._2
}

trait Direction
object Direction {
  case object OUT extends Direction
  case object IN extends Direction
  case object BOTH extends Direction
}

trait Element {
  val id: Any
  val label: String
  val graph: Graph
  /** Returns set of property keys */
  def keys: Set[String]
  /** Returns property */
  def property[V](key: String): Property[V]
  /** Set or update property */
  def property[V](key: String, value: V): Property[V]
  /** Returns property value */
  def value[V](key: String): Option[V]
  def remove(): Unit = {}

  /** Returns property iterator */
  def properties[V]: Iterator[Property[V]] = {
    keys.map(k => property[V](k)).iterator
  }
  /** Returns property value iterator */
  def values[V]: Iterator[V] = {
    keys.map(k => value[V](k).get).iterator
  }

  def copy_props(dest: Element): Unit = {
    properties.foreach((p: Property[_]) => p.value match {
      case Some(x) => dest.property(p.key, x)
      case None =>
    })
  }
}

trait Vertex extends Element {
  /** Returns adjacent vertices */
  def vertices(direction: Direction, edge_labels: Seq[String]): Iterator[Vertex]
  /** Returns edges attached to the vertex */
  def edges(direction: Direction, edge_labels: Seq[String]): Iterator[Edge]
  /** Adds a new edge to the graph */
  def add_edge(label: String, to: Vertex, key_values: (String, Any)*): Edge
}

trait Edge extends Element {
  def vertices(direction: Direction): Iterator[Vertex]
  val out_vertex: Vertex
  val in_vertex : Vertex
  def both_vertices: Iterator[Vertex] = vertices(Direction.BOTH)
}

trait Property[+V] {
  val key: String
  val value: Option[V]
  val element: Element
  def is_present: Boolean = value.isDefined
  def or_else[SV >: V](other: => SV): SV = value.getOrElse(other)
  def empty: Property[V] = Property.empty
  def remove(): Unit
}

object Property {
  val _empty: Property[Nothing] = new Property[Nothing] {
    val key = ""
    val value = None
    val element = null
    def remove(): Unit = {}
  }

  def empty[V]: Property[V] = _empty
}

trait Graph {
  def add_vertex(label: String, key_values: (String, Any)*): Vertex
//  def add_vertex(label: String): Vertex[IdType] = add_vertex(PropId.label -> label)
  def traversal(): GraphTraversalSource = new GraphTraversalSource(this, Bytecode())
  def vertices(vertex_ids: Any*): Iterator[Vertex]
  def edges(edge_ids: Any*): Iterator[Edge]
  def copy: Graph
  def print_all(pfn: String => Unit = println): Unit = {
    vertices().foreach(v => {
      val prop_str = v.properties
        .filter((p: Property[_]) => p.value.isDefined)
        .map((p: Property[_]) => s"${p.key}: ${p.value.get}")
        .mkString(", ")
      pfn(s"V[${v.id}: ${v.label}]{$prop_str}")
    })

    edges().foreach(e => {
      val prop_str = e.properties
        .filter((p: Property[_]) => p.value.isDefined)
        .map((p: Property[_]) => s"${p.key}: ${p.value.get}")
        .mkString(", ")
      pfn(s"V[${e.out_vertex.id}->[${e.id}: ${e.label}]->${e.in_vertex.id}]{$prop_str}")
    })

  }
}
