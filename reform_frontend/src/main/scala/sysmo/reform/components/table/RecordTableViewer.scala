package sysmo.reform.components.table

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent

import scala.scalajs.js
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.table.aggrid.AgGridComponent
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import sysmo.reform.shared.field.{Value, FieldType}
import sysmo.reform.shared.table2.{Table, TableService, SelectionHandler}

object RecordTableViewer extends ReactComponent {
  import japgolly.scalajs.react._

  trait Props {
    type F[_]
    val ds: TableService[F]
    val source: Q.QuerySource
    val schema: Table.Schema
    val height: String
    val selection_handler: Option[SelectionHandler]
  }
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def schema2columns(schema: Table.Schema): Seq[agf.ColumnProps] = {
      schema.fields.map(field => {
        val filter = field.ftype match {
          case FieldType.Char => Some(agf.Filters.text)
          case FieldType.Int => Some(agf.Filters.number)
          case FieldType.Real => Some(agf.Filters.number)
          case _ => None
        }

        val value_getter: agf.ValueGetter = (x : agf.ValueGetterParams) => {
          x.data.toOption match {
            case Some(Some(row_data)) => row_data.get(field.name) match {
              case y if y.is_na => "N/A"
              case y: Value.DateValue => {
                val date = y.as_date.get
                (new scala.scalajs.js.Date(date.getTime)).toDateString()

              }
              case y => y.get.asInstanceOf[js.Any]
            }
            case _ => "N/A!!!"
          }
        }

        agf.column(
          field.name,
          value_getter = Some(value_getter),
          headerName = Some(field.make_descr),
          filter = filter, sortable = Some(true)
        )
      })
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        AgGridComponent(
          p.ds, p.source, p.schema,
          schema2columns(p.schema),
          p.height,
          p.selection_handler
        )
      )
    }
  }

  def component[U] =
    ScalaComponent.builder[Props]("RecordTableViewer")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply[T[_]](_ds : TableService[T], _schema: Table.Schema, _source: Q.QuerySource,
                  _height: String = "800px", _selection_handler: Option[SelectionHandler]): Unmounted = {
    component(new Props{
      type F[X] = T[X]
      val ds = _ds
      val source = _source
      val schema = _schema
      val height = _height
      val selection_handler = _selection_handler
    })
  }
}
