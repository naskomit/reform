package sysmo.reform.shared.query

import sysmo.reform.shared.expr.PredicateExpression
import sysmo.reform.shared.types.RecordFieldType

object SQLModel {

  case class Column(id: String,
                    table: Option[String],
                    alias: Option[String])
  case class Select(columns: Seq[Column])
  case class Table(id: String, alias: Option[String] = None)
  case class From(table: Table)
  case class Where(expr: PredicateExpression)
  case class ColumnOrder(col: String, ascending: Boolean)
  case class Order(column_orders: Seq[ColumnOrder])

  case class SQL(select: Select,
                 from: From,
                 where: Option[Where],
                 order: Option[Order],
                 ftypes: Seq[Option[RecordFieldType]]
                )

  case class TextualQuery(q: String, args: Seq[Any], ftypes: Seq[Option[RecordFieldType]])

  def apply(select: Select, from: From): SQL = SQL(select, from, None, None, Seq())
}
