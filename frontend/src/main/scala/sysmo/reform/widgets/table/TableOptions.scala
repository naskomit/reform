package sysmo.reform.widgets.table

import sysmo.reform.effects.{CopyToClipboard, NotifySuccess}
import sysmo.reform.widgets.table.ColumnOptions.ColumnOptionsBuilder
import sysmo.reform.shared.actions.Action
import sysmo.reform.shared.data.Value
import Value.implicits._
import sysmo.reform.shared.logging.Logging
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
                          cell_formatter: Option[CellFormatter] = None,
                          cell_actions: CellActions,
                          tooltip: Option[Boolean] = None,
                        )

object ColumnOptions {
  type Modifier = ColumnOptionsBuilder => ColumnOptionsBuilder
  class ColumnOptionsBuilder(var current: ColumnOptions) {
    def cell_formatter(v: CellFormatter): this.type = {
      current = current.copy(cell_formatter = Some(v))
      this
    }

    def update_actions(f: CellActions => CellActions): this.type = {
      current = current.copy(cell_actions = f(current.cell_actions))
      this
    }

    def on_click(f: Value => Action): this.type = {
      update_actions(_.copy(click = Some(f)))
    }

    def on_click_copy(): this.type = {
      update_actions(_.copy(click = Some(
        v => if (v.is_set) {
          CopyToClipboard(v) + NotifySuccess(s"Copied ${v.get[String].get} to clipboard")
        } else {
          Action.None
        }
      )))
    }

    def sortable(v: Boolean = true): this.type = {
      current = current.copy(sortable = Some(v))
      this
    }
    def filter(v: ColumnFilter): this.type = {
      current = current.copy(filter = Some(v))
      this
    }

    def tooltip(v: Boolean = true): this.type = {
      current = current.copy(tooltip = Some(v))
      this
    }

    def build(): ColumnOptions = current
  }

  def builder(id: String): ColumnOptionsBuilder =
    new ColumnOptionsBuilder(ColumnOptions(id, cell_actions = CellActions()))

  def builder(ftype: RecordFieldType): ColumnOptionsBuilder = {
    val bld = builder(ftype.name)
    val renderer = ftype.dtype match {
      case PrimitiveDataType.Id => IdCellFormatter
      case PrimitiveDataType.Date => DateCellFormatter
      case _ => TextCellFormatter
    }
    bld.cell_formatter(renderer)

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
  type Modifier = TableOptionsBuilder => TableOptionsBuilder
  class TableOptionsBuilder(
    var current: TableOptions,
    val column_builders: SequenceIndex[String, ColumnOptionsBuilder]
  ) extends Logging {

    def column(key: String): Option[ColumnOptionsBuilder] = column_builders.get(key)

    def columns(mod: ColumnOptions.Modifier): this.type = {
      for (bld <- column_builders.toSeq) {
        mod(bld)
      }
      this
    }

    def columns(keys: Seq[String], mod: ColumnOptions.Modifier): this.type = {
      for (key <- keys) {
        column_builders.get(key) match {
          case Some(bld) => mod(bld)
          case None => logger.warn(s"Column $key not found")
        }
      }
      this
    }

    def modify(mod: Modifier): this.type = {
      mod(this)
      this
    }

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
