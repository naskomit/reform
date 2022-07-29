//package sysmo.reform.components.plots
//
//import io.circe.Json
//import japgolly.scalajs.react.vdom.html_<^._
//import org.scalajs.dom
//import org.scalajs.dom.html
//import sysmo.reform.components.ReactComponent
//import sysmo.reform.shared.{chart => Ch}
//import sysmo.reform.util.json.circe_2_js
//
//import scala.scalajs.js
//import scala.scalajs.js.annotation.JSImport
//
//object PlotlyNative {
//
//  @JSImport("plotly.js", JSImport.Default)
//  @js.native
//  object Plotly extends js.Object {
//    // (element: html.Element, definition: js.Object)
//    val newPlot: js.Function2[html.Element, js.Object, js.Promise[_]] = js.native
//    val react: js.Function2[html.Element, js.Object, js.Promise[_]] = js.native
//  }
//
//}
//
//object Plotly extends ReactComponent {
//  import japgolly.scalajs.react._
//
//  case class Props(width: String, height: String, uid: String, content: Json)
//  case class State()
//
//  final class Backend($: BackendScope[Props, State]) {
//    private val outerRef = Ref[html.Element]
//    def render (p: Props, s: State): VdomElement = {
//      <.div(^.style:= js.Dictionary("width" -> p.width, "height" -> p.height)).withRef(outerRef)
//    }
//
//    def show(p: Props): Callback = Callback {
//      logger.info(s"Running plotly show with uid = ${p.uid}")
//      outerRef.raw.current match {
//        case elem : html.Element => {
//          PlotlyNative.Plotly.react(elem, circe_2_js(p.content))
//        }
//      }
//      ()
//    }
//  }
//
//  implicit val props_reuse = Reusability.by((_ : Props).uid)
//  implicit val state_reuse = Reusability.derive[State]
//
//  // : Scala.Component[Props, State, Backend, _]
//  val component =
//    ScalaComponent.builder[Props]("Plotly")
//    .initialState(State())
//    .renderBackend[Backend]
//    .componentDidMount(f => f.backend.show(f.props))
//    .componentDidUpdate(f => f.backend.show(f.currentProps))
//    .configure(Reusability.shouldComponentUpdate)
//    .build
//
//  // , definition: js.Object
//  def apply(width: String, height: String, plt_def: Ch.Plotly): Unmounted = {
//    component(Props(width, height, plt_def.uid, plt_def.content))
//  }
//}
