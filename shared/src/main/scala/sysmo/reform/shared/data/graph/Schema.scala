package sysmo.reform.shared.data.graph

import scala.collection.mutable
import sysmo.reform.shared.util.{Named, TNamed}

sealed trait PropType
case class StringType() extends PropType
case class IntegerType() extends PropType
case class RealType() extends PropType
case class BoolType() extends PropType
case class DateType() extends PropType
case class DateTimeType() extends PropType
//case class RefType(to: EntitySchema) extends PropType

sealed trait Domain
//case class Category(value: String, label: Option[String])
case class CategoricalDomain(categories: Option[Seq[Named]] = None) extends Domain

object CategoricalDomain {
  def unlabelled(categories: Seq[String]): CategoricalDomain =
    CategoricalDomain(Some(categories.map(x => Named(x, Some(x)))))
}

case class Prop
(
  name: String, label: Option[String],
  prop_type: PropType,
  multiplicity: Int,
  domain: Option[Domain]
)

object Prop {
  class Builder(name: String, prop_type: PropType) {
    private var prop = Prop(
      name = name, label = None, prop_type = prop_type,
      multiplicity = 1, domain = None
    )
    def label(value: String): Builder = {
      prop = prop.copy(label = Some(value))
      this
    }

    def mult(value: Int): Builder = {
      prop = prop.copy(multiplicity = value)
      this
    }
    def categorical: Builder = {
      prop = prop.copy(domain = Some(CategoricalDomain()))
      this
    }
    def categorical[T: TNamed](items: T*): Builder = {
      val categories = items.map {
        case x: String => Named(x, Some(x))
        case x: Named => x
        case x => throw new IllegalArgumentException("Either string or Category must be passed to categorical")
      }.toSeq
      prop = prop.copy(domain = Some(CategoricalDomain(Some(categories))))
      this
    }
    def build: Prop = {
      // check
      prop
    }



  }

  def int(name: String) = new Builder(name, IntegerType())
  def real(name: String) = new Builder(name, RealType())
  def bool(name: String) = new Builder(name, BoolType())
  def string(name: String) = new Builder(name, StringType())
  def date(name: String) = new Builder(name, DateType())
  def datetime(name: String) = new Builder(name, DateTimeType())
//  def ref(name: String, to: EntitySchema) = new Builder(name, RefType(to))
}

case class Link(name: String, label: Option[String], to: VertexSchema,
           schema: Option[EdgeSchema], multiplicity: Int)

object Link {
  class Builder(name: String, to: VertexSchema) {
    var _schema: Option[EdgeSchema] = None
    var link = Link(name = name, label = None, to = to,
      schema = _schema, multiplicity = 1)
    def schema(schema: EdgeSchema): this.type = {
      _schema = Some(schema)
      this
    }
    def label(value: String): this.type = {
      link = link.copy(label = Some(value))
      this
    }
    def build: Link = link
  }

  def builder(name: String, to: VertexSchema): Builder = new Builder(name, to)
}

sealed trait EntitySchema {
  val name: String
  val props: Seq[Prop]
  private val prop_map = props.zipWithIndex.map({case (prop, index) => (prop.name, index)}).toMap
//  def field(index: Int): Field = fields(index)
  def prop(name: String): Option[Prop] = prop_index(name).map(index => props(index))
  def prop_index(name: String): Option[Int] = prop_map.get(name)
}

case class VertexSchema(name: String, props: Seq[Prop], links: Seq[Link])
    extends EntitySchema {
  private val link_map = links.zipWithIndex.map({case (link, index) => (link.name, index)}).toMap
//  def link(index: Int): Link = links(index)
  def link(name: String): Option[Link] = link_index(name).map(index => links(index))
  def link_index(name: String): Option[Int] = link_map.get(name)

}
case class EdgeSchema(name: String, props: Seq[Prop])
  extends EntitySchema

object Schema {
  def table_schema_builder(schema: VertexSchema) = Graph2TableSchema.builder(schema)

  trait EntityBuilder {
    protected val props = new mutable.ArrayBuffer[Prop]
    def prop(bld: Prop.Builder): this.type = {
      props += bld.build
      this
    }
  }

  class VertexBuilder(name: String) extends EntityBuilder {
    protected val links = new mutable.ArrayBuffer[Link]()
    def link(bld: Link.Builder): this.type = {
      links += bld.build
      this
    }
    def build: VertexSchema = VertexSchema(name, props.toSeq, links.toSeq)
  }

  class EdgeBuilder(name: String) extends EntityBuilder {
    def build: EntitySchema = EdgeSchema(name, props.toSeq)
  }



  def vertex_builder(name: String) = new VertexBuilder(name)
  def edge_builder(name: String) = new EdgeBuilder(name)
}

