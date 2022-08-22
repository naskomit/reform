package sysmo.reform.explorers

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.runtime.ObjectRuntime

class RuntimeExplorerF[F[+_]] extends ReactComponent {


  case class Props(runtime: ObjectRuntime[F])
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div("This is a template component")
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(runtime: ObjectRuntime[F]): Unmounted = {
    component(Props(runtime))
  }
}

//package sysmo.reform.explorers
//
//import slinky.core.{BaseComponentWrapper, ComponentWrapper, DefinitionBase, KeyAndRefAddingStage, StateReaderProvider, StateWriterProvider}
//import slinky.core.facade.ReactElement
//import slinky.readwrite.Reader
//import sysmo.reform.shared.runtime.ObjectRuntime
//
//import scala.scalajs.js
//
//// (implicit sr: StateReaderProvider, sw: StateWriterProvider)
//// (implicit sr: StateReaderProvider, sw: StateWriterProvider)
//abstract class RuntimeExplorerF(implicit sr: StateReaderProvider, sw: StateWriterProvider) extends BaseComponentWrapper(sr, sw) {
//  import slinky.web.{html => <}
//  case class _Props(runtime: Int)
//  case class State()
//  override type Definition = DefinitionBase[Props, State, Snapshot]
////  implicit val propsReader: Reader[Props] = (o: js.Object) => Props(null)
////  type Props = TypedProps[_]
//  class Def(js_props: js.Object) extends Definition(js_props) {
//    override def initialState: State = State()
//    override def render(): ReactElement = <.div(
//      <.h1("Runtime Explorer"),
//    )
//  }
//}
//
////object RuntimeExplorer {
////  def apply[F[+_]](runtime: ObjectRuntime[F]): KeyAndRefAddingStage[RuntimeExplorer[F]#Def] = {
////    val component = new RuntimeExplorer[F] {}
////    component(component.Props(runtime))
////  }
////}