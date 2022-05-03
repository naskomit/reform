package sysmo.reform.components.menu

import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent

import scala.scalajs.js



object ButtonToolbar extends ReactComponent {
  import japgolly.scalajs.react._

  type CB = AsyncCallback[Unit]
  sealed trait ToolbarControl
  case class Button(name: String, cb: CB) extends ToolbarControl

  case class Props(controls: Seq[ToolbarControl], margin: Int = 5)
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "wrapper", ^.style:= js.Dictionary("background" -> "#DDD"),
        p.controls.map {
          case Button(name, cb) => <.button(^.key:= name,
            ^.cls := "btn btn-primary", ^.style:= js.Dictionary("margin-left" -> p.margin, "margin-right" -> p.margin)
            , name, ^.onClick --> cb)
        }.toTagMod

      )
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
//  def apply(): Unmounted = {
//    component(Props())
//  }

  class Builder {
    private var controls = Seq[Button]()
    def button(name: String, cb: CB): this.type = {
      controls = controls :+ new Button(name, cb)
      this
    }
    def build = component(Props(controls))
  }

  def builder = new Builder()
}
