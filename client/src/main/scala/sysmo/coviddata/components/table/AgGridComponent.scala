package sysmo.coviddata.components.table

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.coviddata.components.table.{AgGridFacades => agf}

import scala.scalajs.js
import js.JSConverters._
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

trait TableDatasource[U] {
  type RemoteBatch = Future[Seq[U]]
  var total_rows : Int = -1

  def rowCount : Future[Int]
  def getRows(params: agf.IGetRowsParams): RemoteBatch

  def toNative() : agf.TableDatasource = {
    val ds = (new js.Object).asInstanceOf[agf.TableDatasource]
//    ds.rowCount = () => total_rows
    ds.getRows = params => {
      dom.console.log(params)
      getRows(params).foreach(
        data => params.successCallback(data.toJSArray, total_rows)
      )
    }
    ds
  }
  val native = toNative()
  rowCount.foreach(x => total_rows = x)
}

object AgGridComponent {
  import japgolly.scalajs.react.component.Scala.BackendScope
  import japgolly.scalajs.react.ScalaComponent
  case class Props(ds : TableDatasource[_], columns : Seq[AgGridColumn])
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
            ^.height := "800px",
            ^.width := "100%",
            agf.AgGridNativeComponent(p.ds.native, p.columns)
          )
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("AgGrid")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(ds : TableDatasource[_], columns : Seq[AgGridColumn]) =
    component(Props(ds, columns))

}
