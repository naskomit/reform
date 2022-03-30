package sysmo.reform.components.table

import japgolly.scalajs.react.component.Scala.RawMounted
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.data.TableDatasource
import sysmo.reform.shared.data.table.Schema
import sysmo.reform.shared.{query => Q}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object PredefinedTableViewer extends ReactComponent {

  import japgolly.scalajs.react._

  case class Props(table_data_source: TableDatasource, table_id: String)
  case class State(table_schema: Option[Schema], table_id_loaded: String)

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls:= "page-title",
          <.h1(p.table_id)
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          s.table_schema match{
            case Some(schema) =>
              if (s.table_id_loaded == p.table_id)
                RecordTableViewer(p.table_data_source, schema, Q.SingleTable(p.table_id))
              else
                <.div("Reloading!")
            case None => <.div("No schema!")
          }
        )
      )
    }

    def load_schema(p: Props): AsyncCallback[Unit] = AsyncCallback.fromFuture(
      p.table_data_source.table_schema(p.table_id)
    ).flatMap(schema => $.modState(s => s.copy(table_schema = Some(schema), table_id_loaded = p.table_id)).asAsyncCallback)
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State(None, ""))
    .renderBackend[Backend]
    .componentDidMount(f => f.backend.load_schema(f.props))
    .componentDidUpdate(update => {
      if (update.prevProps.table_id != update.currentProps.table_id)
        update.backend.load_schema(update.currentProps)
      else
        AsyncCallback.fromFuture(Future.successful())
    })
    .build

  def apply(app_config: ApplicationConfiguration, table_id: String) =
    component(Props(app_config.table_source, table_id))
}
