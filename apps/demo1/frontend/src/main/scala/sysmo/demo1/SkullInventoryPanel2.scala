package sysmo.demo1

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Configuration, Panel}
import sysmo.reform.shared.runtime.RemoteRuntime
import sysmo.reform.service.RemoteHttpService
import sysmo.reform.shared.containers.{FLocal, FRemote}
import sysmo.reform.explorers.RecordExplorerF
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.query.{BasicQuery, Fields, Query, SingleTable}
import sysmo.reform.shared.types.{RecordType, TypeSystem}


object SkullInventoryPanel2 extends Panel {
  object RecordExlorer extends RecordExplorerF[FRemote]

  case class Props()
  case class State(runtime: Option[RemoteRuntime[FRemote]])

  final class Backend($: BScope) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls := "page-title",
          <.h1("Skull Inventory")
        ),
        <.div(^.cls := "wrapper wrapper-white",
          s.runtime match {
            case Some(runtime) => RecordExlorer(runtime, "SkullSample")
            case None => <.div("Initializing runtime ...")
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


  def create_query(ts: TypeSystem): Query = {
    val SkullSample = ts.get("SkullSample").get.asInstanceOf[RecordType]
    val fields = Seq("code", "sex", "age", "image_type")
      .map { name =>
        val ftype = SkullSample.field(name)
        Expression.field(name, ftype)
      }


    val query = BasicQuery(
      source = SingleTable("SkullSample"),
      projection = Fields(fields)
    )

    query
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