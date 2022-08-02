package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.editors.AbstractFormComponent
import sysmo.reform.components.forms.layouts.ArrayChildElement
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.table.{RecordTableViewer, TableSelectionHandler}
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.{query => Q}
import sysmo.reform.shared.data.{NoFilter, TableService, table => T}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.components.layouts.{NamedContent, TabbedLayout}
import sysmo.reform.shared.data.table.{Row, Schema}
import sysmo.reform.shared.form.build.{FieldGroup, GroupUnion}

import scala.concurrent.Future

object ArrayBrowser extends ReactComponent {
  class SelectionHandler() extends TableSelectionHandler {
    //      override val id_columns: Seq[String] = _
    override val mode: TableSelectionHandler.RowSelectionMode = TableSelectionHandler.SingleRow
    override def on_change(selection: Iterable[Row]): Unit = {
      if (selection.size == 1) {

      }
    }
  }

  case class Props(obj: FR.Array, table_service: TableService, options: FormRenderingOptions)
  case class State(schema: Seq[T.Schema], selection_handler: Option[SelectionHandler])
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      if (s.schema.nonEmpty) {
        TabbedLayout(s.schema.map(schema =>
          NamedContent(schema.name,
            RecordTableViewer(
              p.table_service, schema, Q.SingleTable(schema.name), "400px", s.selection_handler
            )
          )
        ))
      } else {
        <.div("Loading schema")
      }
    }

    object Effects {
      def append(p: Props, concrete_type: Option[String] = None): ButtonToolbar.CB = Callback {
        p.obj.runtime.dispatch(FR.AppendElement(p.obj.id, concrete_type))
      }.asAsyncCallback
    }
  }

  def get_schemas(p: Props, mod_state: (State => State) => Callback): AsyncCallback[Unit] = {
    AsyncCallback.fromFuture{
      p.table_service.list_tables(NoFilter)
        .flatMap(tables =>
          Future.sequence(tables.map { table_id=>
            p.table_service.table_schema(table_id.name)
          })
        )
    }.flatMap(schema_list => mod_state(s => s.copy(schema = schema_list)).asAsyncCallback)

  }

  val component =
    ScalaComponent.builder[Props]("ArrayBrowser")
      .initialState(State(Seq(), None))
      .renderBackend[Backend]
      .componentDidMount(
        f => f.modState(s => s.copy(selection_handler = Some(new SelectionHandler))).asAsyncCallback
          >> get_schemas(f.props, f.modState)
      )
      .componentDidUpdate{f =>
        if (f.currentProps.obj != f.prevProps.obj) {
          f.modState(s => s.copy(schema = Seq())).asAsyncCallback >> get_schemas(f.currentProps, f.modState)
        } else {
          AsyncCallback.unit
        }
      }
      .build

  def apply(field_obj: FR.Array, options: FormRenderingOptions): Unmounted = {
    val table_service: TableService = FR.TableView(field_obj)
    component(Props(field_obj, table_service, options))
  }
}
