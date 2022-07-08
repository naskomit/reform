package sysmo.reform.components.menu

import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent

import scala.scalajs.js


sealed trait ButtonLayout
case object Linear extends ButtonLayout
case class Dropdown(label: VdomElement) extends ButtonLayout
object Dropdown {
  def apply(label: String): Dropdown = new Dropdown(<.span(label))
}

object ButtonToolbar extends ReactComponent {

  import japgolly.scalajs.react._

  type CB = AsyncCallback[Unit]
  sealed trait ToolbarControl
  case class Button(name: String, cb: CB) extends ToolbarControl

  case class Props(controls: Seq[ToolbarControl], button_layout: ButtonLayout, margin: Int = 5) {
    def component: Unmounted = ButtonToolbar.component(this)
  }
  case class State(expanded: Boolean)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      p.button_layout match {
        case Linear => <.span(^.className:= "wrapper", ^.background:= "#EEE",
          p.controls.map {
            case Button(name, cb) => <.button(^.key:= name,
              ^.cls := "btn btn-primary", ^.style:= js.Dictionary("marginLeft" -> p.margin, "marginRight" -> p.margin)
              , name, ^.onClick --> cb)
          }.toTagMod
        )

        case Dropdown(label) => {
//          val data_toggle = VdomAttr("data-toggle")
          // , <.span(^.cls:= "caret")
          <.span(^.classSet1("dropdown", "open" -> s.expanded),
            <.button(^.cls := "btn btn-primary dropdown-toggle", ^.`type`:= "button",
              label, ^.onClick -->
                $.modState(s => s.copy(expanded = !s.expanded))
            ),
            <.ul(^.className:= "dropdown-menu",
              p.controls.map {
                case Button(name, cb) => <.li(
                  ^.role:= "presentation",
                  <.a(^.role:= "menuitem", ^.tabIndex:= -1, ^.href:="#",
                      ^.onClick ==> (e => Callback[Unit] {
                        e.preventDefault()
                      }.asAsyncCallback >>
                        $.modState(s => s.copy(expanded = false)).asAsyncCallback >>
                        cb),
                    name
                  )
                )
              }.toTagMod
            )
          )
        }
      }
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ButtonToolbar")
    .initialState(State(false))
    .renderBackend[Backend]
    .build

  //
//  def apply(): Unmounted = {
//    component(Props())
//  }

  class Builder {
    private var button_layout: ButtonLayout = Linear
    private var controls = Seq[Button]()

    def button(name: String, cb: CB): this.type = {
      controls = controls :+ new Button(name, cb)
      this
    }

    def dropdown(label: String): this.type = {
      button_layout = Dropdown(label)
      this
    }

    def dropdown(label: VdomElement): this.type = {
      button_layout = Dropdown(label)
      this
    }

    def build: Props = Props(controls, button_layout)
  }

  def builder = new Builder()
}
