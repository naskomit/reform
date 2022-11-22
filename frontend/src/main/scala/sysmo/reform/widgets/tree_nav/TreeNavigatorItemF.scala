package sysmo.reform.widgets.tree_nav

import cats.MonadThrow
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.shared.sources.{tree => T}
import sysmo.reform.css.{ReFormCSS => CSS}
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.shared.sources.tree.TreeNode

object TreeNavigatorItemF {
  case class Settings(
                       base_padding: Int = 8, level_offset: Int = 24,
                       height: Int = 28
                     ) {
    def indent(level: Int): Int = level_offset
  }
}

class TreeNavigatorItemF[F[+_]](implicit f2c: F2Callback[F])
  extends ReactComponent {
  object ContextMenu extends ContextMenuF[F, TreeNode[F]#ActionType]
  val Component = this
  type Settings = TreeNavigatorItemF.Settings

  case class State(expanded: Boolean, children: Seq[T.TreeNode[F]])
  case class Props(node: T.TreeNode[F], level: Int, settings: Settings)

  final class Backend($: BackendScope[Props, State]) {
    def toggle_expanded(p: Props)(e: ReactEvent): AsyncCallback[Unit] = {
      toggle_expanded2(p){
        e.stopPropagation()
      }
    }

    def toggle_expanded2(p: Props)(initial_effect: => Unit): AsyncCallback[Unit] = {
        initial_effect
        p.node match {
          case n: T.TreeBranch[F] => f2c.async(n.children.traverse())
          case n: T.TreeLeaf[F] => f2c.async_pure(Seq())
        }
      }.flatMap(
        children => $.modStateAsync(s => s.copy(expanded = !s.expanded, children = children))
      )

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
              case n: T.TreeBranch[F] => <.div(CSS.tree_nav.item_expand, if (s.expanded) "-" else "+")
              case n: T.TreeLeaf[F] => <.div(CSS.tree_nav.item_expand_leaf)

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
            ContextMenu(p.node.actions, p.node.dispatcher)
          )
        ),
        <.div(CSS.tree_nav.item_children,
          s.children.map(x =>
            apply(x, p.level + 1, p.settings)
          ).toTagMod
//          (p.node match {
//            case n: T.TreeBranch[TT, F] => n.children.map((x =>
//              Component.apply(x, p.level + 1, p.settings)
//            )).toTagMod
//            case n: T.TreeLeaf[TT, F] => <.span()
//          })
        ).when(s.expanded)

      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("TreeNavigatorItem")
      .initialState(State(false, Seq()))
      .renderBackend[Backend]
      .componentDidMount(f => if (f.props.level < 2) {
        f.backend.toggle_expanded2(f.props){}
      } else {
        (Callback {}).asAsyncCallback
      })
      .build

  def apply(_node: T.TreeNode[F], _level: Int, _settings: Settings): Unmounted =
    component(Props(_node, _level, _settings))

}