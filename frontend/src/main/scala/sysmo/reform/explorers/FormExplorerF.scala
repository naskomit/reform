package sysmo.reform.explorers

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.react.ReactComponent
import sysmo.reform.react.layouts.{NamedContent, TabbedLayout}
import sysmo.reform.shared.runtime.{RFObject, RFRuntime}

class FormExplorerF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  object InstanceTable extends InstanceTableF[F]
  object ObjectTreeBrowser extends ObjectTreeBrowserF[F]

  case class Props(root_obj: F[RFObject[F]])

  case class State(current_object: Option[RFObject[F]], runtime: Option[RFRuntime[F]])

  final class Backend($: BScope) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls := "page-title",
          <.h1("Runtime Explorer")
        ),
        <.div(^.cls := "wrapper wrapper-white",
          (s.current_object, s.runtime) match {
            case (Some(current_object), Some(runtime)) =>
              TabbedLayout(
                NamedContent("Tree Browser", ObjectTreeBrowser(current_object, runtime)),
                NamedContent("Instance Table", InstanceTable(runtime))
              )

            case _ => <.div("Initializing...")
          }
        )
      )
    }

    def start(p: Props): AsyncCallback[Unit] = for {
      obj <- f2c.async(p.root_obj)
      res <- $.modStateAsync(s =>
        s.copy(current_object = Some(obj), runtime = Some(obj.get_runtime))
      )
    } yield res
  }

  val component = ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State(None, None))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.start(f.props))
    .build

  def apply(root_obj: F[RFObject[F]]): Unmounted = {
    component(Props(root_obj))
  }
}
