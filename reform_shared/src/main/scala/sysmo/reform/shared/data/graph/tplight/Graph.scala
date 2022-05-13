package sysmo.reform.shared.data.graph.tplight

import scala.language.implicitConversions

trait PropId

object PropId {
  case object label extends PropId {
    override def toString: String = "LABEL"
  }
  case object id extends PropId {
    override def toString: String = "ID"
  }
  case object key extends PropId {
    override def toString: String = "KEY"
  }

  case object value extends PropId {
    override def toString: String = "VALUE"
  }
  case class StringId(v: String) extends PropId

  implicit def Str2PropId(x: Tuple2[String, Any]): Tuple2[PropId, Any] =
    PropId.StringId(x._1) -> x._2
}

trait Direction
object Direction {
  case object OUT extends Direction
  case object IN extends Direction
  case object BOTH extends Direction
}

trait Element[IdType] {
  val id: IdType
  val label: String
  val graph: Graph[IdType]
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
}

trait Vertex[IdType] extends Element[IdType] {
  def add_edge(label: String, to: Vertex[IdType], key_values: Tuple2[PropId, Any]*): Edge[IdType]
}

trait Edge[IdType] extends Element[IdType] {
  def vertices(direction: Direction): Seq[Vertex[IdType]]
  val out_vertex: Vertex[IdType]
  val in_vertex : Vertex[IdType]
  def both_vertices: Seq[Vertex[IdType]] = vertices(Direction.BOTH)
}

trait Property[+V] {
  val key: String
  val value: Option[V]
  val element: Element[_]
  def is_present: Boolean = value.isDefined
  def or_else[SV >: V](other: => SV): SV = value.getOrElse(other)
  def empty: Property[V] = Property.empty
  def remove(): Unit = {}
}

object Property {
  val _empty: Property[Nothing] = new Property[Nothing] {
    val key = ""
    val value = None
    val element = null
  }

  def empty[V]: Property[V] = _empty
}

trait Graph[ID] {
  type IdType = ID
  def add_vertex(label: String, key_values: Tuple2[PropId, Any]*): Vertex[IdType]
//  def add_vertex(label: String): Vertex[IdType] = add_vertex(PropId.label -> label)
  def traversal(): GraphTraversalSource[IdType] = new GraphTraversalSource[IdType](this)
  def vertices(vertex_ids: IdType*): Iterator[Vertex[IdType]]
  def edges(edge_ids: IdType*): Iterator[Edge[IdType]]
}
