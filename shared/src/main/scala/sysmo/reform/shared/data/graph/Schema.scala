package sysmo.reform.shared.data.graph

import scala.collection.mutable
import sysmo.reform.shared.util.{Named, TNamed}
import sysmo.reform.shared.data.{Property, PropType, StringType, IntegerType, RealType, BoolType, DateType, DateTimeType}

//case class RefType(to: EntitySchema) extends PropType

//sealed trait Domain
////case class Category(value: String, label: Option[String])
//case class CategoricalDomain(categories: Option[Seq[Named]] = None) extends Domain
//
//object CategoricalDomain {
//  def unlabelled(categories: Seq[String]): CategoricalDomain =
//    CategoricalDomain(Some(categories.map(x => Named(x, Some(x)))))
//}
//
//case class Prop
//(
//  name: String, label: Option[String],
//  prop_type: PropType,
//  multiplicity: Int,
//  domain: Option[Domain]
//)



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
  val props: Seq[Property]
  private val prop_map = props.zipWithIndex.map({case (prop, index) => (prop.name, index)}).toMap
//  def field(index: Int): Field = fields(index)
  def prop(name: String): Option[Property] = prop_index(name).map(index => props(index))
  def prop_index(name: String): Option[Int] = prop_map.get(name)
}

case class VertexSchema(name: String, props: Seq[Property], links: Seq[Link])
    extends EntitySchema {
  private val link_map = links.zipWithIndex.map({case (link, index) => (link.name, index)}).toMap
//  def link(index: Int): Link = links(index)
  def link(name: String): Option[Link] = link_index(name).map(index => links(index))
  def link_index(name: String): Option[Int] = link_map.get(name)

}
case class EdgeSchema(name: String, from: EdgeSchema, to: EdgeSchema, props: Seq[Property])
  extends EntitySchema

object Schema {
  def table_schema_builder(schema: VertexSchema) = Graph2TableSchema.builder(schema)

  trait EntityBuilder {
    protected val props = new mutable.ArrayBuffer[Property]
    def prop(bld: Property.Builder): this.type = {
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

  class EdgeBuilder(name: String, from: EdgeSchema, to: EdgeSchema) extends EntityBuilder {
    def build: EntitySchema = EdgeSchema(name, props.toSeq)
  }



  def vertex_builder(name: String) = new VertexBuilder(name)
  def edge_builder(name: String) = new EdgeBuilder(name)
}

