package sysmo.reform.components.plots

import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.html
import sysmo.reform.components.ReactComponent

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

  case class Props(width: String, height: String, definition: js.Object)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    private val outerRef = Ref[html.Element]
    def render (p: Props, s: State): VdomElement = {
      <.div(^.style:= js.Dictionary("width" -> p.width, "height" -> p.height)).withRef(outerRef)
    }

    def init(p: Props): Callback = Callback {
      outerRef.raw.current match {
        case elem : html.Element =>
          PlotlyNative.Plotly.newPlot(elem, p.definition)
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
  def apply(width: String, height: String): Unmounted = {
    val definition =
      """
        |{"data":[{"type":"histogram","x":[1.0,1.0,4.0,2.0,1.0,4.0,2.0,1.0,3.0]}],"layout":{"title":"Bars"},"config":{}}
      """.stripMargin
    val json = js.JSON.parse(definition).asInstanceOf[js.Object]
    component(Props(width, height, json))
  }
}
