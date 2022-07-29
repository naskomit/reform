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
  trait Props {
    type TT <: T.TreeTypes
    val actions: Seq[T.NodeAction[TT#ActionType]]
    val dispatcher: T.Dispatcher[TT]
  }
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
        if (s.expanded)
          close(event)
        else
          open(event)
      }

    def fire_action[U <: T.TreeTypes](dispatcher: T.Dispatcher[U], a: T.NodeAction[_])(event: ReactEvent): Callback = {
      Callback {
        event.stopPropagation()
      } >> $.modState(s => s.copy(expanded = false)) >> Callback {
        dispatcher.dispatch(a.data.asInstanceOf[U#ActionType])
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

  def apply[T <: T.TreeTypes](_actions: Seq[T.NodeAction[T#ActionType]], _dispatcher: T.Dispatcher[T]): Unmounted =
    component(new Props {
      type TT = T
      val actions = _actions
      val dispatcher = _dispatcher
    })
}

object TreeNavigatorItem extends ReactComponent {
  case class Settings(
                       base_padding: Int = 8, level_offset: Int = 24,
                       height: Int = 28
                     ) {
    def indent(level: Int): Int = level_offset
  }
  case class State(expanded: Boolean)
  trait Props {
    type TT <: T.TreeTypes
    val node: T.TreeNode[TT]
    val level: Int
    val settings: Settings
  }

  final class Backend($: BackendScope[Props, State]) {
    def toggle_expanded(p: Props)(e: ReactEvent): Callback = Callback {
      e.stopPropagation()
    } >> $.modState(s => s.copy(expanded = !s.expanded))

    def toggle_selected(p: Props)(e: ReactEvent): Callback = Callback {
      e.stopPropagation()
      p.node.dispatcher.select(p.node.id)
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(CSS.tree_nav.nav_item,
        ^.paddingLeft:= p.settings.indent(p.level).px,

        <.div(CSS.tree_nav.row,
          CSS.tree_nav.row_selected.when(p.node.is_selected),

          <.div(
            ^.onClick ==> toggle_expanded(p),
            p.node match {
              case n: T.TreeBranch[_] => <.div(CSS.tree_nav.item_expand, if (s.expanded) "-" else "+")
              case n: T.TreeLeaf[_] => <.div(CSS.tree_nav.item_expand_leaf)

            },
          ),

          <.div(
            ^.flex:= "1",
            ^.onClick ==> toggle_selected(p),
            <.div(CSS.tree_nav.item_icon,
              <.i(^.cls:= p.node.icon.getOrElse("fa fa-circle-check"))
            ),
            <.div(CSS.tree_nav.item_name,
              p.node.name,
            ),
          ),

          <.div(CSS.tree_nav.item_context_menu,
            ContextMenu[p.TT](p.node.actions, p.node.dispatcher)
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

  def apply[T <: T.TreeTypes](_node: T.TreeNode[T], _level: Int, _settings: Settings): Unmounted =
    component(new Props {
      type TT = T
      val node = _node
      val level = _level
      val settings = _settings
    })

}


object TreeNavigatorComponent extends ReactComponent {
  case class State()
  trait Props {
    type TT <: T.TreeTypes
    val data: T.Tree[TT]
  }

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

  def apply[T <: T.TreeTypes](_data: T.Tree[T]): Unmounted =
    component(new Props {
      type TT = T
      val data = _data
    })

}

