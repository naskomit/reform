package sysmo.reform.explorers

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.runtime.RFRuntime
import RFRuntime.implicits._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.react.table.TableViewerF
import sysmo.reform.shared.query.{QuerySource, SingleTable}
import sysmo.reform.shared.table.{Table, TableService}

class InstanceTableF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {


  case class Props(runtime: RFRuntime[F], source: QuerySource)
  case class State(schema: Option[Table.Schema])
  object TableViewer extends TableViewerF[F]

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      val ts = p.runtime: TableService[F]
      s.schema match {
        case Some(sch) => TableViewer(ts, sch, p.source, "800px", None)
        case None => <.div("Loading table schema")
      }
    }

    def start(p: Props): AsyncCallback[Unit] = for {
      sch <- {
        val ts = p.runtime: TableService[F]
        f2c.async(ts.table_schema(""))
      }
      su <- $.modStateAsync(s => s.copy(schema = Some(sch)))
    } yield su
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State(None))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.start(f.props))
    .build

  //
  def apply(runtime: RFRuntime[F]): Unmounted = {
    component(Props(runtime, SingleTable("")))
  }
}
