package sysmo.reform.components.table

import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.table.aggrid.AgGridComponent
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import sysmo.reform.data.TableDatasource
import sysmo.reform.shared.data.{table => sdt}

object RecordTableViewer {
  import japgolly.scalajs.react._

  case class Props(ds: TableDatasource, source: Q.QuerySource, schema: sdt.Schema, height: String)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def schema2columns(schema: sdt.Schema): Seq[agf.ColumnProps] = {
      schema.fields.map(field => {
        val filter = field.field_type.tpe match {
          case sdt.VectorType.Char => Some(agf.Filters.text)
          case sdt.VectorType.Int => Some(agf.Filters.number)
          case sdt.VectorType.Real => Some(agf.Filters.number)
          case _ => None
        }

        val value_getter: agf.ValueGetter = (x : agf.ValueGetterParams) => {
          //              println("Value getter")
          //              dom.console.log(x)
          x.data.toOption match {
            case Some(Some(row_data)) => row_data.get(field.name) match {
              case y if y.is_na => "N/A"
              case y => {
                val transformed = field.field_type.ext_class match {
                  case Some("date") => {
                    val date = y.as_date.get
                    (new scala.scalajs.js.Date(date.getTime)).toDateString()
                  }
                  case None => y.v.get
                }
                transformed.asInstanceOf[js.Any]
              }
            }
            case _ => "N/A!!!"
          }
        }

        agf.column(
          field.name,
          value_getter = Some(value_getter),
          headerName = field.label.orElse(Some(field.name)),
          filter = filter, sortable = Some(true)
        )
      })
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        AgGridComponent(
          p.ds, p.source, p.schema,
          schema2columns(p.schema),
          p.height
        )
      )
    }
  }

  def component[U] =
    ScalaComponent.builder[Props]("RecordTableViewer")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(ds : TableDatasource, schema: sdt.Schema, source: Q.QuerySource, height: String = "800px") = {
    component(Props(ds, source, schema, height))
  }
}
