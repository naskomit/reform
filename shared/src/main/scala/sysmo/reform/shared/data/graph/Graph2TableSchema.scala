package sysmo.reform.shared.data.graph

import sysmo.reform.shared.data.{table => T}
import sysmo.reform.shared.data.{graph => G}

object Graph2TableSchema {
  class SchemaVertex2TableBuilder(schema: VertexSchema) {

    def prop2field(prop: G.Prop): T.Field = {
      val tpe = prop.prop_type match {
        case StringType() => T.VectorType.Char
        case IntegerType() => T.VectorType.Int
        case RealType() => T.VectorType.Real
        case BoolType() => T.VectorType.Bool
        case DateType() => T.VectorType.Real
        case DateTimeType() => T.VectorType.Real
        case _ => throw new IllegalArgumentException(f"Cannot handle prop $prop")
      }
      val ext_class = prop.prop_type match {
        case DateType() => Some("date")
        case DateTimeType() => Some("datetime")
        case _ => None
      }
      val field_type = T.FieldType(tpe, ext_class = ext_class)
      T.Field(name = prop.name, field_type = field_type, label = prop.label)
    }

    def build: T.Schema = {
      T.Schema(fields = schema.props.map(prop2field))
    }
  }

  def builder(schema: VertexSchema): SchemaVertex2TableBuilder = new SchemaVertex2TableBuilder(schema)
}
