package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.data.table.{Field, Schema, VectorType, table_manager}
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.shared.data.{OptionFilter, TableService, table => sdt}
import sysmo.reform.shared.form.build.{FieldGroup, GroupUnion}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.util.{LabeledValue, Named}

import scala.concurrent.{ExecutionContext, Future}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.query.BasicQuery

object TableView {
  class ArrayAsTableSource(array: Array)(implicit ec: ExecutionContext) extends TableService {
    override type TableIdType = String
    val element_prototype = array.prototype.prototype
    override def list_tables(optionFilter: OptionFilter): Future[Seq[Named]] = {
      println(s"list_tables")
      element_prototype match {
        case group: FieldGroup => Future(Seq(Named(group.symbol, None)))
        case union: GroupUnion => Future(union.subtypes.map(fg => Named(fg.symbol, None)))
      }
    }

    // Schema(name: String, label: Option[String], fields: Seq[Field]
    // Field(name: String, field_type: FieldType, label: Option[String] = None)
    // case class FieldType(tpe: VectorType.Value, nullable: Boolean = true)
    private def create_schema(group: FieldGroup): sdt.Schema = sdt.Schema(
      group.symbol, None, group.field_relations.map{ rel =>
        val col_type: VectorType.Value = rel.child_field match {
          case x: FB.StringField => sdt.VectorType.Char
          case x: FB.IntegerField => sdt.VectorType.Int
          case x: FB.FloatField => sdt.VectorType.Real
          case x: FB.BooleanField => sdt.VectorType.Bool
          case x => sdt.VectorType.Char
        }
        sdt.Field(rel.name, sdt.FieldType(col_type))
      }
    )


    override def table_schema(table_id: TableIdType): Future[sdt.Schema] = {
      println(s"table_schema($table_id)")
      val schema: Option[Schema] = element_prototype match {
        case group: FieldGroup => Some(create_schema(group))
        case union: GroupUnion => union.subtypes.find(g => g.symbol == table_id).map(create_schema)
      }
      schema match {
        case Some(x) => Future(x)
        case None => Future.failed(new IllegalArgumentException(s"Unknown type $table_id"))
      }
    }

    private def vector_value(v: SomeValue[_]): sdt.Value[_] = {
      v.v.value match {
        case x: Int => sdt.Value.int(x)
        case x: Double => sdt.Value.real(x)
        case x: String => sdt.Value.char(x)
        case x: Boolean => sdt.Value.bool(x)
        case x => ???
      }
    }


    override def query_table(q: Q.Query): RemoteBatch = {
      println(s"query_table($q)")
      val schema_req = q match {
        case BasicQuery(Q.SingleTable(id, _, schema), columns, filter, sort, range) => table_schema(id)
        case _ => ???
      }
      schema_req.map{schema =>
        val builder = sdt.table_manager.incremental_table_builder(schema)
        array.children.foreach(child_id =>
          array.runtime.get_typed[Group](child_id) match {
            case Some(child) => {
              val row: Map[String, sdt.Value[_]] = schema.fields.map { field =>
                val field_value: sdt.Value[_] = child.child(field.name).flatMap(array.runtime.get).map{
                  case AtomicValue(prototype, value, id, parent_rel) => value match {
                    case NoValue => sdt.Value.empty
                    case AllValues => ???
                    case v: SomeValue[_] => vector_value(v)
                    case MultiValue(v) => sdt.Value.char("...")
                  }
                  case g: Group => sdt.Value.char(s"[${g.prototype.symbol}]")
                  case a: Array => sdt.Value.char(s"Array[${a.prototype.prototype.symbol}]")
                  case r: Reference => sdt.Value.char(r.ref_id.toString)
                }.getOrElse(sdt.Value.empty)
                (field.name, field_value)
              }.toMap
              builder.append_value_map(row)
            }
            case None =>
          }
        )
        builder.toTable
      }
    }
  }

  def apply(array: Array)(implicit ec: ExecutionContext): TableService =
    new ArrayAsTableSource(array)(ec)
}
