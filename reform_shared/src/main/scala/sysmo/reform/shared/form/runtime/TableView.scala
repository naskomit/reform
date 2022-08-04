package sysmo.reform.shared.form.runtime

import cats.MonadThrow
import sysmo.reform.shared.field.{Field, FieldType, Schema, Value}
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.util.{LabeledValue, Named}

import scala.concurrent.{ExecutionContext, Future}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.query.BasicQuery
import sysmo.reform.shared.table2.{Table, TableService}

object TableView {
  private def create_field(rel: FB.HasElement): Field = new Field {
    override def name: String = rel.name
    override def descr: Option[String] = Some(rel.descr)
    override def ftype: FieldType = rel.child_field match {
      case x: FB.StringField => FieldType.Char
      case x: FB.IntegerField => FieldType.Int
      case x: FB.FloatField => FieldType.Real
      case x: FB.BooleanField => FieldType.Bool
      case x => FieldType.Char
    }
    override def nullable: Boolean = true
  }

  private def oid_field(group: FB.FieldGroup): Field = new Field {
    override def name: String = "$objectid"
    override def descr: Option[String] = Some("Unique object ID")
    override def ftype: FieldType = FieldType.Char
    override def nullable: Boolean = false
  }

  class GroupAsTableSchema(group: FB.FieldGroup) extends Table.Schema {
      val extra_fields = Seq(oid_field(group))
      val _field_map: Map[String, Int] = (
        group.field_relations.zipWithIndex.map(x => (x._1.name, x._2)) ++
          extra_fields.zipWithIndex.map(x => (x._1.name, x._2 + group.field_relations.size))
        ).toMap
      override def symbol: String = group.symbol
      override def fields: Seq[Field] = group.field_relations.map(create_field) :+ oid_field(group)
      override def field(index: Int): Option[Field] =
        if (index >= 0 && index < group.field_relations.size)
          Some(create_field(group.field_relations(index)))
        else if (index >= group.field_relations.size && index < group.field_relations.size + extra_fields.size)
          Some(extra_fields(index - group.field_relations.size))
        else
          None
      override def field_index(name: String): Option[Int] = _field_map.get(name)
    }

  class GroupAsTableRow(group: Group, val schema: Table.Schema) extends Table.Row {
    private def vector_value(v: SomeValue[_]): Value[_] = {
      v.v.value match {
        case x: Int => Value.int(x)
        case x: Double => Value.real(x)
        case x: String => Value.char(x)
        case x: Boolean => Value.bool(x)
        case x => ???
      }
    }
//    override def schema: Schema = new GroupAsTableSchema(group.prototype)

    //        array.children.foreach(child_id =>
    //          array.runtime.get_typed[Group](child_id) match {
    //            case Some(child) => {
    //              val row: Map[String, sdt.Value[_]] = schema.fields.map { field =>
    //                val field_value: sdt.Value[_] = child.child(field.name).flatMap(array.runtime.get).map{
    //                  case AtomicValue(prototype, value, id, parent_rel) => value match {
    //                    case NoValue => sdt.Value.empty
    //                    case AllValues => ???
    //                    case v: SomeValue[_] => vector_value(v)
    //                    case MultiValue(v) => sdt.Value.char("...")
    //                  }
    //                  case g: Group => sdt.Value.char(s"[${g.prototype.symbol}]")
    //                  case a: Array => sdt.Value.char(s"Array[${a.prototype.prototype.symbol}]")
    //                  case r: Reference => sdt.Value.char(r.ref_id.toString)
    //                }.getOrElse(sdt.Value.empty)
    //                (field.name, field_value)
    //              }.toMap
    //              builder.append_value_map(row + ("$objectid" -> sdt.Value.int(child_id.id)))
    //            }
    //            case None =>
    //          }
    //        )
    override protected def _get(col: Int): Value[_] = {
      schema.field(col).flatMap(field => group.child(field.name)).flatMap(group.runtime.get).map{
        case AtomicValue(prototype, value, id, parent_rel) => value match {
          case NoValue => Value.empty()
          case AllValues => Value.char("ALL")
          case v: SomeValue[_] => vector_value(v)
          case MultiValue(v) => Value.char("[...]")
        }
        case g: Group => Value.char(s"[${g.prototype.symbol}]")
        case a: Array => Value.char(s"Array[${a.prototype.prototype.symbol}]")
        case r: Reference => Value.char(r.ref_id.toString)
      }.getOrElse(Value.empty())
    }
  }

  class ArrayAsTableSource(array: Array)(implicit ec: ExecutionContext) extends TableService[Future] {
    override val mt: MonadThrow[Future] = MonadThrow.apply[Future]
    override type TableIdType = String
    val element_prototype = array.prototype.prototype
    override def list_tables(): Future[Seq[Named]] = {
      println(s"list_tables")
      element_prototype match {
        case group: FB.FieldGroup => Future(Seq(Named(group.symbol, None)))
        case union: FB.GroupUnion => Future(union.subtypes.map(fg => Named(fg.symbol, None)))
      }
    }

    override def table_schema(table_id: TableIdType): Future[Table.Schema] = {
      println(s"table_schema($table_id)")
      val schema: Option[Table.Schema] = element_prototype match {
        case group: FB.FieldGroup => Some(new GroupAsTableSchema(group))
        case union: FB.GroupUnion => union.subtypes.find(g => g.symbol == table_id).map(g => new GroupAsTableSchema(g))
      }
      schema match {
        case Some(x) => Future(x)
        case None => Future.failed(new IllegalArgumentException(s"Unknown type $table_id"))
      }
    }

    override def query_table(q: Q.Query): Future[Table] = {
      println(s"query_table($q)")
      val schema_req = q match {
        case BasicQuery(Q.SingleTable(id, _, schema), columns, filter, sort, range) => table_schema(id)
        case _ => ???
      }

      schema_req.map(sch => new Table {
        override def schema: Table.Schema = sch
        override def nrow: Int = array.count
        override def get(row_id: Int, col_id: Int): Value[_] = row(row_id).get(col_id)
        override def row(row_id: Int): Table.Row = row_iter.toSeq(row_id)
        override def row_iter: Iterator[Table.Row] = array.element_iterator.map(g => new GroupAsTableRow(g, sch))
      })
    }
  }

  def apply(array: Array)(implicit ec: ExecutionContext): TableService[Future] =
    new ArrayAsTableSource(array)(ec)
}
