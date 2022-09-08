package sysmo.reform.react.tree_nav

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import scalacss.ScalaCssReact._
import org.scalajs.dom
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.sources.{tree => T}
import scala.scalajs.js
import sysmo.reform.css.{ReFormCSS => CSS}

class ContextMenuF[TT <: T.TreeTypes, F[+_]] extends ReactComponent {
  type HandlerFn[T <: dom.Event] = js.Function1[T, _]

  case class State(expanded: Boolean)

  case class Props(actions: Seq[T.NodeAction[TT#ActionType]], dispatcher: T.Dispatcher[TT, F])

  final class Backend($: BackendScope[Props, State]) {
    private val outer_ref = Ref[dom.html.Element]

    def open(e: ReactEvent): Callback = $.modState { s =>
      if (e.eventType == "click") {
        dom.window.dispatchEvent(new dom.Event("click"))
      }
      dom.window.addEventListener("click", close_native)
      s.copy(expanded = true)

    }

    val close_native: HandlerFn[dom.Event] = e => $.modState { s =>
      dom.window.removeEventListener("click", close_native)
      s.copy(expanded = false)
    }.runNow()

    def close(event: ReactEvent): Callback = Callback {
      close_native(event.nativeEvent)
    }

    def toggle(s: State)(event: ReactEvent): Callback = Callback {
      event.stopPropagation()
    } >> {
      if (s.expanded)
        close(event)
      else
        open(event)
    }

    def fire_action(dispatcher: T.Dispatcher[TT, F], a: T.NodeAction[_])(event: ReactEvent): Callback = {
      Callback {
        event.stopPropagation()
      } >> $.modState(s => s.copy(expanded = false)) >> Callback {
        dispatcher.dispatch(a.data.asInstanceOf[TT#ActionType])
      }
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.i(^.cls := "fa fa-bars",
          ^.onClick ==> toggle(s),
        ),
        <.div(
          CSS.tree_nav.context_menu_container,
          p.actions.map(a =>
            <.div(
              CSS.tree_nav.context_menu_item,
              a.name,
              ^.onClick ==> fire_action(p.dispatcher, a)
            )
          ).toTagMod
        ).when(s.expanded)
      ).withRef(outer_ref)
    }
  }

  val component =
    ScalaComponent.builder[Props]("ContextMenu")
      .initialState(State(false))
      .renderBackend[Backend]
      .build

  def apply(_actions: Seq[T.NodeAction[TT#ActionType]], _dispatcher: T.Dispatcher[TT, F]): Unmounted =
    component(Props(_actions, _dispatcher))
}