package sysmo.reform.explorers

import cats.MonadThrow
import cats.syntax.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.widgets.tree_nav.TreeNavigatorComponentF
import sysmo.reform.widgets.property.PropertyGroupEditorF
import sysmo.reform.shared.runtime.{ArrayInstance, RFObject, RFRuntime}
import sysmo.reform.shared.sources.{tree => T}

class ObjectTreeBrowserF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  object TreeNavigatorComponent extends TreeNavigatorComponentF[F]
  object PropertyGroupEditor extends PropertyGroupEditorF[F]
  object ArrayTableViewer extends ArrayTableViewerF[F]

  case class Props(obj: RFObject[F], runtime: RFRuntime[F])
  case class State(render_id: Int, tree_view: Option[RFObject.TreeView[F]], prop_obj: Option[RFObject[F]])


  final class Backend($: BackendScope[Props, State]) {
    object R extends T.Renderer {
      def reset_prop_obj(): AsyncCallback[Unit] = $.modStateAsync{s =>
        val new_prop_obj = (s.tree_view, s.prop_obj) match {
          case (Some(tree_view), Some(obj)) if (tree_view.selection.head != obj.id) => None
          case _ => s.prop_obj
        }
        s.copy(prop_obj = new_prop_obj)
      }

      def set_prop_obj(): AsyncCallback[Unit] = {
        for {
          props <- $.props.asAsyncCallback
          state <- $.state.asAsyncCallback
          result <- {
            (state.tree_view, state.prop_obj) match {
              case (Some(tree_view), None) =>
                f2c.async(
                  props.runtime.get(tree_view.selection.head)
                ).flatMap(obj => $.modStateAsync(s => s.copy(prop_obj = Some(obj))))
              case _ => AsyncCallback.unit
            }
          }

        } yield result
      }

      override def rerender(): Unit =
        (reset_prop_obj() >> set_prop_obj()).runNow()

    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        ^.display:= "flex", ^.columnGap:= 100.px,
        <.div(^.minWidth:= 300.px, ^.maxWidth:= 400.px,
          ^.flexGrow:= "1",
          s.tree_view match {
            case Some(tree_view) => TreeNavigatorComponent(tree_view)
            case None => <.div("Initializing")
          }
        ),
        <.div( ^.flexGrow:= "3",
          <.h2("Properties"),
          s.prop_obj match {
            case Some(obj: ArrayInstance[F]) => ArrayTableViewer(obj)
            case Some(prop_obj) => PropertyGroupEditor(RFObject.NamedPropertyView(prop_obj))
            case None => <.div("No properties")
          }
          //        val prop_view = RFObject.NamedPropertyView(tree_view.selection.head)

        )
      )
    }

    def start(props: Props): Callback = $.modState { s =>
      val tree_view = new RFObject.TreeView[F](props.obj)
      tree_view.renderer = Some(R)
      s.copy(tree_view = Some(tree_view), prop_obj = Some(props.obj))
    }
  }

  val component =
    ScalaComponent.builder[Props]("RuntimeExplorer")
      .initialState(State(0, None, None))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.start(f.props))
      .build

  //
  def apply(obj: RFObject[F], runtime: RFRuntime[F]): Unmounted = {
    component(Props(obj, runtime))
  }
}
