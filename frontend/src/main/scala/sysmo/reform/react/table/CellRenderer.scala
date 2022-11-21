package sysmo.reform.react.table

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.app.PanelManager
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.data.Value.implicits._
import sysmo.reform.react.{ReactComponent, Tooltip}

import java.util.Date

class CellRenderer(formatter: CellFormatter) extends ReactComponent {
  case class Props(v: Value)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      PanelManager.ctx.consume(panel_manager =>
        Tooltip()(
          <.div(
            ^.onClick ==> on_click(p, panel_manager),
            formatter.render(p.v)
          ),
          <.div("Tooltip")
        )
      )
    }

    def on_click(p: Props, panel_manager: PanelManager)(e: ReactEvent): Callback = Callback {
      panel_manager.toast(s"Hello from Cell ${p.v.get[String].get}")
    }
  }
  val component: Scala.Component[Props, State, Backend, CtorType.Props] =
    ScalaComponent.builder[Props]("CellRenderer")
      .initialState(State())
      .renderBackend[Backend]
      .build

  //
  def apply(v: Value): Unmounted = {
    component(Props(v))
  }

//  type jsProps = sysmo.reform.react.table.aggrid.AgGridFacades.ICellRendererParams
//  val jsComponent = ScalaComponent.builder[jsProps]
//    .render_P { props =>
//      apply(props.value)
//    }
//    .build
//    .toJsComponent
}

object CellRenderer {
  def apply(formatter: CellFormatter): CellRenderer =
    new CellRenderer(formatter)
}

trait CellFormatter {
  def render(v: Value): VdomElement = {
    render_formatted(
      format(v)
    )
  }
  def on_click(e: ReactEvent): Callback = Callback {
    e.preventDefault()
    e.stopPropagation()
    dom.console.log("clicked")
  }
  def format(v: Value): Option[String]
  def format_na: String = "<N/A>"
  def render_formatted(x: Option[String]): VdomElement = {
    x match {
      case Some(value) => <.span(x)
      case None => <.span(format_na)
    }
  }
}

object IdCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[ObjectId].map(_.show)
}

object TextCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[String]
}

object DateCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[Date].map(date =>
      (new scala.scalajs.js.Date(date.getTime)).toDateString()
    )
}


object LinkCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] = None
  override def render(v: Value): VdomElement = {
    case class Props()
    <.i(^.cls:="fa-solid fa-link")
  }
}

