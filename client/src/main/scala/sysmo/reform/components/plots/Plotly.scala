package sysmo.reform.components.plots

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.html
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{chart => Ch}
import sysmo.reform.util.json.circe_2_js

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object PlotlyNative {

  @JSImport("plotly.js", JSImport.Default)
  @js.native
  object Plotly extends js.Object {
    // (element: html.Element, definition: js.Object)
    val newPlot: js.Function2[html.Element, js.Object, js.Promise[_]] = js.native
  }

}

object Plotly extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(width: String, height: String, plt_def: Ch.Plotly)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    private val outerRef = Ref[html.Element]
    def render (p: Props, s: State): VdomElement = {
      <.div(^.style:= js.Dictionary("width" -> p.width, "height" -> p.height)).withRef(outerRef)
    }

    def init(p: Props): Callback = Callback {
      outerRef.raw.current match {
        case elem : html.Element => {
          PlotlyNative.Plotly.newPlot(elem, circe_2_js(p.plt_def.content))
        }
      }
      ()
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.init(f.props))
    .build

  // , definition: js.Object
  def apply(width: String, height: String, plt_def: Ch.Plotly): Unmounted = {
    component(Props(width, height, plt_def))
  }
}
