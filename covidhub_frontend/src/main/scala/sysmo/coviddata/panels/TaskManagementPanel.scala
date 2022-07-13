package sysmo.coviddata.panels

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.components.forms.FormEditorComponent
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._


object TaskManagementPanel extends ApplicationPanel {
  case class Props(group: FR.Group) // form: F.FieldGroup, data_handler: FormDataHandler
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      FormEditorComponent(p.group)
    }
  }

  val component =
    ScalaComponent.builder[Props]("RecursiveFormPanel")
      .initialState(State())
      .renderBackend[Backend]
      .build

  val build_graph = MemGraph()


  val TaskBuilder = FB.FieldGroup.builder(build_graph, "Task")
  val TaskGroupBuilder = FB.FieldGroup.builder(build_graph, "TaskGroup")
  val PersonBuilder = FB.FieldGroup.builder(build_graph, "Person")
  val StatusBuilder = FB.FieldGroup.builder(build_graph, "Status")

  val Person = PersonBuilder
    .field(_("name").descr("Name"), _.char)
    .field(_("position").descr("Position"), _.char)
    .field(_("cid").descr("Company ID"), _.char.unique())
    .build

  val Status = StatusBuilder
    .field(_("name").descr("Name"), _.char)
    .build

  val Task = TaskBuilder
    .field(_("description").descr("Description"), _.char)
    .ref(_("status").descr("Status"), _(StatusBuilder, "name").label_expr(E.field("name")))
    .ref(_("responsible").descr("Responsible"), _(PersonBuilder, "cid").label_expr(E.field("name")))
    .ref(_("participants").descr("Participants"), _(PersonBuilder, "cid").multiple().label_expr(E.field("name")))
    .field(_("duration").descr("Duration"), _.char)
    .build


  val TaskGroup = TaskGroupBuilder
    .field(_("description").descr("Description"), _.char)
    .array(_("tasks").descr("Tasks"), TaskBuilder)
    .array(_("groups").descr("Subgroups"), TaskGroupBuilder)
    .build

  val TaskManagement = FB.FieldGroup.builder(build_graph, "TaskManagement")
    .array(_("people").descr("People"), PersonBuilder)
    .array(_("statuses").descr("Statuses"), StatusBuilder)
    .group(_("tasks").descr("Tasks"), TaskGroupBuilder)
    .layout("tabbed")
    .build

//  import FR.Group.FieldDef._
  val runtime = FR.FormRuntime(build_graph)
  import FR.instantiation._


  val task_management = runtime.instantiate(
    TaskManagement(
      "people" -> Seq(
        Person("name" -> "John Doe", "position" -> "CEO"),
        Person("name" -> "Old Sam", "position" -> "Developer"),
        Person("name" -> "Young Sam", "position" -> "Intern"),
      ),
      "statuses" -> Seq(
        Status("name" -> "Backlog"),
        Status("name" -> "To Do"),
        Status("name" -> "In Progress"),
        Status("name" -> "Done"),
      ),
      "tasks" -> TaskGroup(
        "description" -> "Top-level task group",
      )
    )
  )

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(task_management.asInstanceOf[FR.Group])) // form, data_handler
  }
}

