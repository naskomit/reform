package sysmo.reform.shared.data.graph

import sysmo.reform.shared.data.Property
import sysmo.reform.shared.util.INamed

import scala.collection.mutable

sealed trait EntitySchema extends INamed {
  val props: Seq[Property]
  private val prop_map = props.zipWithIndex.map({case (prop, index) => (prop.name, index)}).toMap
  def prop(name: String): Option[Property] = prop_index(name).map(index => props(index))
  def prop_index(name: String): Option[Int] = prop_map.get(name)
}

case class VertexSchema(name: String, label: Option[String], props: Seq[Property])
    extends EntitySchema {
}

trait Multiplicity
case object MultOne extends Multiplicity
case object MultOptOne extends Multiplicity
case object MultMany extends Multiplicity

case class EdgeSchema(
  name: String, label: Option[String],
  props: Seq[Property],
  from: OVCRef, from_mult: Multiplicity,
  to: OVCRef, to_mult: Multiplicity
) extends EntitySchema

object Schema {
  def table_schema_builder(schema: VertexSchema): Graph2TableSchema.SchemaVertex2TableBuilder = Graph2TableSchema.builder(schema)

  trait EntityBuilder {
    protected val name: String
    protected var _label: Option[String] = None
    protected val props = new mutable.ArrayBuffer[Property]
    def label(value: String): this.type = {
      _label = Some(value)
      this
    }
    def prop(bld: Property.Builder): this.type = {
      props += bld.build
      this
    }
  }

  class VertexSchemaBuilder(val name: String) extends EntityBuilder {
    def build: VertexSchema = VertexSchema(name, _label, props.toSeq)
  }

  class EdgeSchemaBuilder(val name: String) extends EntityBuilder {
    protected var _from: OVCRef = None
    protected var _mult_from: Multiplicity = MultMany
    protected var _to: OVCRef = None
    protected var _mult_to: Multiplicity = MultMany
    def from(value: VCRef, mult: Multiplicity = MultMany): this.type = {
      _from = Some(value)
      _mult_from = mult
      this
    }
    def to(value: VCRef, mult: Multiplicity = MultMany): this.type = {
      _to = Some(value)
      _mult_to = mult
      this
    }
    def build: EdgeSchema = EdgeSchema(
      name, _label, props.toSeq,
      _from, _mult_from, _to, _mult_to
    )
  }



  def vertex_builder(name: String) = new VertexSchemaBuilder(name)
  def edge_builder(name: String) = new EdgeSchemaBuilder(name)
}

trait DatabaseSchema {
  trait VertexClass extends VCRef {def _uid: String = target.name}
  trait EdgeClass extends ECRef {def _uid: String = target.name}
  val vertex_schemas: Seq[VertexClass]
  val edge_schemas: Seq[EdgeClass]
  protected lazy val vertex_schema_map: Map[String, VertexClass] = vertex_schemas.map(x => (x.uid, x)).toMap
  protected lazy val edge_schema_map: Map[String, EdgeClass] = edge_schemas.map(x => (x.uid, x)).toMap
  protected def vertex_class(schema: VertexSchema): VertexClass = new VertexClass {
    override def _target: VertexSchema = schema
  }
  protected def edge_class(schema: EdgeSchema): EdgeClass = new EdgeClass {
    override def _target: EdgeSchema = schema
  }
  def vertex_schema(name: String): Option[VertexSchema] = vertex_schema_map.get(name).map(_.target)
  def edge_schema(name: String): Option[EdgeSchema] = edge_schema_map.get(name).map(_.target)
}