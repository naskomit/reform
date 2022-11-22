package sysmo.reform.explorers

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.widgets.table.{TableOptions, TableViewerF}
import sysmo.reform.shared.containers.RFContainer
import sysmo.reform.shared.query.{QuerySource, SingleTable}
import sysmo.reform.shared.runtime.RFRuntime
import sysmo.reform.shared.table.{Table, TableService}

class RecordExplorerF[F[+T] <: RFContainer[T]](implicit f2c: F2Callback[F]) extends ReactComponent {
  case class Props(runtime: RFRuntime[F], record_symbol: String, table_option_modifier: Option[TableOptions.Modifier])
  case class State(table_service: TableService[F], schema: Option[Table.Schema], source: QuerySource)
  object TableViewer extends TableViewerF[F]

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      s.schema match {
        case Some(schema) => {
          val table_options_builder = TableOptions.builder(schema)
          p.table_option_modifier.foreach{mod =>
            mod(table_options_builder)
          }
          TableViewer(s.table_service, schema, s.source, table_options_builder.build())
        }
        case None => <.div("Loading ...")
      }
    }

    def start(p: Props, s: State): AsyncCallback[Unit] = {
      for {
        sch <- {
          f2c.async(s.table_service.table_schema(p.record_symbol))
        }
        su <- $.modStateAsync(s => s.copy(schema = Some(sch)))
      } yield su
    }
  }

  val component =
    ScalaComponent.builder[Props]("RecordExplorer")
      .initialStateFromProps(p =>
        State(RFRuntime.record_explorer(p.runtime), None, SingleTable(p.record_symbol))
      ).renderBackend[Backend]
      .componentDidMount(f => f.backend.start(f.props, f.state))
      .build

  //
  def apply(runtime: RFRuntime[F], record_symbol: String, table_options_mod: Option[TableOptions.Modifier] = None): Unmounted = {
    component(Props(runtime, record_symbol, table_options_mod))
  }
}
