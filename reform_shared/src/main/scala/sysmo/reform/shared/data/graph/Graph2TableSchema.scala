package sysmo.reform.shared.data.graph

import sysmo.reform.shared.{data => D}
import sysmo.reform.shared.data.{table => T}
import sysmo.reform.shared.data.{graph => G}


object Graph2TableSchema {
  class SchemaVertex2TableBuilder(schema: VertexSchema, categorical: Boolean) {

    def prop2field(prop: D.Property): T.Field = {
      val tpe = prop.tpe match {
        case D.StringType() => T.VectorType.Char
        case D.IntegerType() => T.VectorType.Int
        case D.RealType() => T.VectorType.Real
        case D.BoolType() => T.VectorType.Bool
        case D.DateType() => T.VectorType.Real
        case D.DateTimeType() => T.VectorType.Real
        case _ => throw new IllegalArgumentException(f"Cannot handle prop $prop")
      }
      val ext_class = (prop.tpe, prop.domain) match {
        case (D.DateType(), _) => T.Date
        case (D.DateTimeType(), _) => T.DateTime
        case (_, Some(D.CategoricalDomain(_))) if categorical => T.Categorical
        case _ => T.Same
      }

      val categories = if (ext_class == T.Categorical) {
        prop.domain match {
          case Some(D.CategoricalDomain(Some(cats))) => cats.map(_.make_label)
          case Some(D.CategoricalDomain(None)) => Seq[String]()
        }
      } else {
        Seq[String]()
      }
      val field_type = T.FieldType(tpe, ext_class = ext_class, categories = categories)
      T.Field(name = prop.name, field_type = field_type, label = prop.label)
    }

    def build: T.Schema = {
      T.Schema(schema.name, schema.label, fields = schema.props.map(prop2field))
    }
  }

  def builder(schema: VertexSchema, categorical: Boolean = false): SchemaVertex2TableBuilder = new SchemaVertex2TableBuilder(schema, categorical)
}
