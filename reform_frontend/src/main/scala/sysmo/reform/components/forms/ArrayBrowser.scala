package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.editors.AbstractFormComponent
import sysmo.reform.components.forms.layouts.ArrayChildElement
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.table.RecordTableViewer
import sysmo.reform.components.menu.ButtonToolbar
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.{query => Q}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.components.layouts.{NamedContent, TabbedLayout}
import sysmo.reform.shared.table2.{SelectionHandler, Table, TableService}
import sysmo.reform.shared.form.build.{FieldGroup, GroupUnion}
import cats.syntax.flatMap
import sysmo.reform.shared.util.Named

import scala.concurrent.Future

object ArrayBrowser extends ReactComponent {
  class SHandler extends SelectionHandler {
    //      override val id_columns: Seq[String] = _
    override val mode = SelectionHandler.SingleRow
    override def on_change(selection: Iterable[Table.Row]): Unit = {
      if (selection.size == 1) {
        println(s"Selected row $selection")
      }
    }
  }

  trait Props {
    val obj: FR.Array
    val table_service: TableService[Future]
    val options: FormRenderingOptions
  }
  case class State(schemas: Seq[Table.Schema], selection_handler: Option[SelectionHandler])
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      if (s.schemas.nonEmpty) {
        TabbedLayout(s.schemas.map(schema =>
          NamedContent(schema.symbol,
            RecordTableViewer(
              p.table_service, schema, Q.SingleTable(schema.symbol), "400px", s.selection_handler
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
      p.table_service.list_tables()
        .flatMap(tables => Future.sequence(tables.map { table_id=>
            p.table_service.table_schema(table_id.name)
        })
      )
    }.flatMap(schema_list => mod_state(s => s.copy(schemas = schema_list)).asAsyncCallback)

  }

  val component =
    ScalaComponent.builder[Props]("ArrayBrowser")
      .initialState(State(Seq(), None))
      .renderBackend[Backend]
      .componentDidMount(
        f => f.modState(s => s.copy(selection_handler = Some(new SHandler))).asAsyncCallback
          >> get_schemas(f.props, f.modState)
      )
      .componentDidUpdate{f =>
        if (f.currentProps.obj != f.prevProps.obj) {
          f.modState(s => s.copy(schemas = Seq())).asAsyncCallback >> get_schemas(f.currentProps, f.modState)
        } else {
          AsyncCallback.unit
        }
      }
      .build

  def apply(_field_obj: FR.Array, _options: FormRenderingOptions): Unmounted = {
    component(new Props{
      val obj = _field_obj
      val table_service = FR.TableView(_field_obj)
      val options = _options
    })
  }
}
