package sysmo.demo1

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.Panel
import sysmo.reform.shared.runtime.RemoteRuntime
import sysmo.reform.service.RemoteHttpService
import sysmo.reform.shared.containers.FRemote
import sysmo.reform.explorers.{RecordExplorerF}
import sysmo.reform.widgets.table.{LinkCellFormatter, TableOptions}
import sysmo.reform.shared.examples.SkullInventoryBuilder
//import sysmo.reform.util.react.Converters._

object SkullInventoryPanel extends Panel {
  object RecordExlorer extends RecordExplorerF[FRemote]

  case class Props()
  case class State(runtime: Option[RemoteRuntime[FRemote]])

  final class Backend($: BScope) {
    import sysmo.reform.shared.data.Value
    val modifier: TableOptions.Modifier = _
      .columns(_.sortable())
      .columns(Seq("Link Soft", "Link Bone"),
          _.cell_formatter(LinkCellFormatter)
            .tooltip().on_click_copy()
      )
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls := "page-title",
          <.h1("Skull Inventory")
        ),
        <.div(^.cls := "wrapper wrapper-white",
          s.runtime match {
            case Some(runtime) =>
              RecordExlorer(runtime, "SkullSample", Some(modifier))
            case _ => <.div("Initializing runtime ...")
          }
        ),
      )
    }

    def init(p: Props): CallbackTo[Unit] = {
      val ts = SkullInventoryBuilder.type_system
      val remote = new RemoteHttpService(Some("api"))
      val runtime = RemoteRuntime(ts, remote)
      $.modState((state, props) => state.copy(runtime = Some(runtime)))
    }
  }

  val component = ScalaComponent.builder[Props]("SkullInventoryPanel2")
    .initialState(State(None))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.init(f.props))
    .build

  def apply(): Unmounted = {
    component(Props())
  }

}


//object SkullInventoryPanel extends Panel {
//  type F[+X] = FLocal[X];
//  object FormExplorer extends FormExplorerF[F]
//
//  case class Props(root_obj: F[RFObject[F]])
//  case class State()
//
//  final class Backend($: BScope) {
//    def render(p: Props, s: State): VdomElement = {
//      FormExplorer(p.root_obj)
//    }
//  }
//
//
//  val component = ScalaComponent.builder[Props]("SkullInventoryPanel")
//    .initialState(State())
//    .renderBackend[Backend]
//    .build
//
//  def apply(): Unmounted = {
//    component(Props(SkullInventoryBuilder.initializer1(LocalRuntimeConstructor).root))
//  }
//
//}