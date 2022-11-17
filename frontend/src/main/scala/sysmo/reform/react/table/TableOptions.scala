package sysmo.reform.react.table

import sysmo.reform.react.table.ColumnOptions.ColumnOptionsBuilder
import sysmo.reform.shared.table.{SelectionHandler, Table}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, MultiReferenceType, PrimitiveDataType, RecordFieldType, ReferenceType}
import sysmo.reform.shared.util.SequenceIndex


sealed trait ColumnFilter
object ColumnFilter {
  object Default extends ColumnFilter
  object Text extends ColumnFilter
  object Number extends ColumnFilter
}

case class ColumnOptions(
                          id: String,
                          header_name: Option[String] = None,
                          filter: Option[ColumnFilter] = None,
                          sortable: Option[Boolean] = None,
                          cell_renderer: Option[CellRenderer] = None
                        )

object ColumnOptions {
  class ColumnOptionsBuilder(var current: ColumnOptions) {
    def cell_renderer(v: CellRenderer): this.type = {
      current = current.copy(cell_renderer = Some(v))
      this
    }
    def filter(v: ColumnFilter): this.type = {
      current = current.copy(filter = Some(v))
      this
    }

    def build(): ColumnOptions = current
  }

  def builder(id: String): ColumnOptionsBuilder =
    new ColumnOptionsBuilder(ColumnOptions(id))

  def builder(ftype: RecordFieldType): ColumnOptionsBuilder = {
    val bld = new ColumnOptionsBuilder(
      ColumnOptions(ftype.name)
    )
    val renderer = ftype.dtype match {
      case PrimitiveDataType.Id => IdCellRenderer
      case PrimitiveDataType.Date => DateCellRenderer
      case _ => TextCellRenderer
    }
    bld.cell_renderer(renderer)

    ftype.dtype match {
      case PrimitiveDataType.Char => bld.filter(ColumnFilter.Text)
      case PrimitiveDataType.Int => bld.filter(ColumnFilter.Number)
      case PrimitiveDataType.Real => bld.filter(ColumnFilter.Number)
      case _ =>
    }
    bld
  }
}
case class TableSize(width: String, height: String)
object TableSize {
  def apply(): TableSize = TableSize("100%", "800px")
}
case class TableOptions(size: TableSize,
                        column_options: Seq[ColumnOptions],
                        selection_handler: Option[SelectionHandler]
                       )

object TableOptions {
  class TableOptionsBuilder(
    var current: TableOptions,
    val column_builders: SequenceIndex[String, ColumnOptionsBuilder]
  ) {

    def column(key: String): Option[ColumnOptionsBuilder] = column_builders.get(key)

    def build(): TableOptions = current.copy(
      column_options = column_builders.toSeq.map(_.build())
    )
  }

  def builder(schema: Table.Schema): TableOptionsBuilder = {
    val column_builders = schema.fields.map(field =>
      ColumnOptions.builder(field)
    )
    new TableOptionsBuilder(
      TableOptions(TableSize(), Seq(), None),
      SequenceIndex(column_builders, _.current.id)
    )
  }
}
