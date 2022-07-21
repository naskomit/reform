package sysmo.reform.components.tree_nav

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.scalajs.dom
import org.scalajs.dom.{Event, MouseEvent, html}
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.{tree => T}
import scalacss.ScalaCssReact._
import sysmo.reform.components.{ReFormCSS => CSS}

import scala.scalajs.js

object ContextMenu extends ReactComponent {
  type HandlerFn[T <: Event] = js.Function1[T, _]
  case class State(expanded: Boolean)
  case class Props(actions: Seq[T.NodeAction[_]], dispatcher: T.Dispatcher[_])
  final class Backend($: BackendScope[Props, State]) {
    private val outer_ref = Ref[html.Element]

    def open(e: ReactEvent): Callback = $.modState {s =>
      if (e.eventType == "click") {
        dom.window.dispatchEvent(new Event("click"))
      }
      dom.window.addEventListener("click", close_native)
      s.copy(expanded = true)

    }

    val close_native: HandlerFn[Event] = e => $.modState {s =>
      dom.window.removeEventListener("click", close_native)
      s.copy(expanded = false)
    }.runNow()

    def close(event: ReactEvent): Callback = Callback {
      close_native(event.nativeEvent)
    }

    def toggle(s: State)(event: ReactEvent): Callback = Callback {
        event.stopPropagation()
      } >> {
        println(s)
        if (s.expanded)
          close(event)
        else
          open(event)
      }

    def fire_action[T](dispatcher: T.Dispatcher[T], a: T.NodeAction[_])(event: ReactEvent): Callback = {
      Callback {
        event.stopPropagation()
      } >> $.modState(s => s.copy(expanded = false)) >> Callback {
        dispatcher.dispatch(a.data.asInstanceOf[T])
      }
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        <.i(^.cls:="fa fa-bars",
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

  def apply(actions: Seq[T.NodeAction[_]], dispatcher: T.Dispatcher[_]): Unmounted = component(Props(actions, dispatcher))
}

object TreeNavigatorItem extends ReactComponent {
  case class Settings(
                       base_padding: Int = 8, level_offset: Int = 24,
                       height: Int = 28
                     ) {
    def indent(level: Int): Int = level_offset
  }
  case class State(expanded: Boolean)
  case class Props(node: T.TreeNode[_], level: Int, settings: Settings)

  final class Backend($: BackendScope[Props, State]) {
    def toggle(p: Props)(e: ReactEvent): Callback = Callback {
      e.stopPropagation()
      p.node.dispatcher.select(p.node.id)
    } >> $.modState(s => s.copy(expanded = !s.expanded))

    def render (p: Props, s: State): VdomElement = {
      <.div(CSS.tree_nav.nav_item,
        ^.paddingLeft:= p.settings.indent(p.level).px,

        <.div(CSS.tree_nav.row,
          CSS.tree_nav.row_selected.when(p.node.is_selected),
          ^.onClick ==> toggle(p),
          p.node match {
            case n: T.TreeBranch[_] => <.div(CSS.tree_nav.item_expand, if (s.expanded) "-" else "+")
            case n: T.TreeLeaf[_] => <.div(CSS.tree_nav.item_expand_leaf)

          },
          <.div(CSS.tree_nav.item_icon,
            <.i(^.cls:= p.node.icon.getOrElse("fa fa-circle-check"))
          ),
          <.div(CSS.tree_nav.item_name,
            p.node.name,
          ),
          <.div(CSS.tree_nav.item_context_menu,
            ContextMenu(p.node.actions, p.node.dispatcher)
          )
        ),
        <.div(CSS.tree_nav.item_children,
          (p.node match {
            case n: T.TreeBranch[_] => n.children.map(x => TreeNavigatorItem(x, p.level + 1, p.settings)).toTagMod
            case n: T.TreeLeaf[_] => <.span()
          })
        ).when(s.expanded)

      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("TreeNavigatorItem")
      .initialStateFromProps(p => State(p.level < 2))
      .renderBackend[Backend]
      .build

  def apply(node: T.TreeNode[_], level: Int, settings: Settings): Unmounted =
    component(Props(node, level, settings))

}


object TreeNavigatorComponent extends ReactComponent {
  case class State()
  case class Props(data: T.Tree[_])

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(CSS.tree_nav.nav,
        TreeNavigatorItem(p.data.root, 0, TreeNavigatorItem.Settings())
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("TreeNavigator")
      .initialState(State())
      .renderBackend[Backend]
      .build

  def apply(data: T.Tree[_]): Unmounted = component(Props(data))

}