package sysmo.demo1

import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Configuration, Panel, PanelManager}
import sysmo.reform.shared.runtime.RemoteRuntime
import sysmo.reform.service.RemoteHttpService
import sysmo.reform.shared.containers.FRemote
import sysmo.reform.explorers.RecordExplorerF
import sysmo.reform.react.table.{LinkCellFormatter, TableOptions}
import sysmo.reform.shared.examples.SkullInventoryBuilder

object SkullInventoryPanel2 extends Panel {
  object RecordExlorer extends RecordExplorerF[FRemote]

  case class Props()
  case class State(runtime: Option[RemoteRuntime[FRemote]])

  final class Backend($: BScope) {
    val modifier: TableOptions.Modifier = { bld =>
      bld.column_builders.toSeq.map(_.sortable())
      bld.column("Link Soft").foreach { col_bld =>
        col_bld.cell_formatter(LinkCellFormatter)
      }
      bld
    }
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
        )
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

  def apply(app_config: Configuration): Unmounted = {
    component(Props())
  }

}