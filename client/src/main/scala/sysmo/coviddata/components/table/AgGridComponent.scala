package sysmo.coviddata.components.table

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.table.AgGridReact.IGetRowsParams

import scala.scalajs.js
import js.JSConverters._

trait TableDatasource[U] {
  def rowCount : Int
  def getRows(params: IGetRowsParams): Seq[U]
  def apply() : AgGridReact.TableDatasource = {
    val ds = (new js.Object).asInstanceOf[AgGridReact.TableDatasource]
    ds.rowCount = () => rowCount
    ds.getRows = params => {
      val data = getRows(params)
      val js_data = data.toJSArray
      params.successCallback(js_data)
    }
    ds
  }
}

object AgGridComponent {
  import japgolly.scalajs.react.component.Scala.BackendScope
  import japgolly.scalajs.react.ScalaComponent


  case class Props(ds : TableDatasource[_])
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.className:="row row-condensed",
          <.div(^.className:="page-subtitle",
            <.div(<.h2("AGGrid")),
          )
        ),
        <.div(^.className:="row",
          <.div(
            ^.cls := "ag-theme-alpine",
            ^.height := "400px",
            ^.width := "600px",
            AgGridReact(p.ds())
          )
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("Counter")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply[U](ds: TableDatasource[U]) = component(Props(ds))

}
