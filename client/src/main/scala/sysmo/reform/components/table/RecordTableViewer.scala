package sysmo.reform.components.table

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.scalajs.js
import sysmo.reform.shared.data.{BoolType, IntegerType, RealType, RecordField, RecordWithMeta, StringType}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.components.table.aggrid.AgGridComponent
import sysmo.reform.components.table.aggrid.{AgGridFacades => agf}
import sysmo.reform.data.TableDatasource
import sysmo.reform.shared.data.{table => sdt}

object RecordTableViewer {
  import japgolly.scalajs.react._

  case class Props[U](ds: TableDatasource, source: Q.QuerySource, schema: sdt.Schema)
  case class State()

  final class Backend[U]($: BackendScope[Props[U], State]) {
    def render (p: Props[U], s: State): VdomElement = {
      <.div(^.style:=js.Dictionary("marginLeft" -> "100px", "marginRight" -> "100px"),
        AgGridComponent(
          p.ds, p.source, p.schema,
          p.schema.fields.map(field => {
            val filter = field.field_type.tpe match {
              case sdt.VectorType.Char => Some(agf.Filters.text)
              case sdt.VectorType.Int => Some(agf.Filters.number)
              case sdt.VectorType.Real => Some(agf.Filters.number)
              case _ => None
            }

            val value_getter: agf.ValueGetter = (x : agf.ValueGetterParams) => {
              println("Value getter")
              dom.console.log(x)
              x.data.toOption match {
                case Some(Some(x)) => x.get(field.name).v.get.asInstanceOf[js.Any]
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
        )
      )
    }
  }

  def component[U] =
    ScalaComponent.builder[Props[U]]("RecordTableViewer")
    .initialState(State())
    .renderBackend[Backend[U]]
    .build

  def apply[U](ds : TableDatasource, table : String)(implicit meta_holder: RecordWithMeta[U]) = {
    val meta = meta_holder._meta
    val fields = meta.field_keys.map(x => {
      val field = meta.fields(x)
      val field_type = field.tpe match {
        case StringType() => sdt.VectorType.Char
        case IntegerType() => sdt.VectorType.Int
        case RealType() => sdt.VectorType.Real
        case BoolType() => sdt.VectorType.Bool
      }
      sdt.Field(field.name, sdt.FieldType(field_type), label = Some(field.label))
    })
    val schema = sdt.Schema(fields)
    val source = Q.SingleTable(table)
    component[U](Props(ds, source, schema))
  }
}
