package sysmo.coviddata.panels

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.components.forms.FormEditorComponent
import sysmo.reform.shared.form.build.GroupUnion
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._


object RecursiveFormPanel extends ApplicationPanel {
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
  val TaskItemBuilder = TaskBuilder.union("TaskItem", _ | TaskGroupBuilder)

  val Person = PersonBuilder
    .field(_("name").descr("Name"), _.char)
    .field(_("position").descr("Position"), _.char)
    .build

  val Task = TaskBuilder
    .field(_("description").descr("Description"), _.char)
    .field(_("responsible").descr("Responsible"), _.char)
    .field(_("duration").descr("Duration"), _.char)
    .build

  val TaskGroup = TaskGroupBuilder
    .field(_("description").descr("Description"), _.char)
    .group(_("t1").descr("Task 1"), TaskBuilder)
    .group(_("t2").descr("Task 2"), TaskBuilder)
    .array(_("other_tasks").descr("Other tasks"), TaskBuilder)
    .array(_("other_items").descr("Other items"), TaskItemBuilder)
//    .layout("tabbed")
    .build

  val TaskManagement = FB.FieldGroup.builder(build_graph, "TaskManagement")
    .array(_("people").descr("People"), PersonBuilder)
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
      "tasks" -> TaskGroup(
        "description" -> "Top-level task group",
        "t1" -> Task("description" -> "Task1", "duration" -> "2d"),
        "t2" -> Task("description" -> "Task2", "duration" -> "4d"),
        "other_tasks" -> Seq(
          Task("description" -> "Task_31", "duration" -> "10h")
        ),
        "other_items" -> Seq(
          Task("description" -> "Task_41", "duration" -> "4h"),
          Task("description" -> "Task_42", "duration" -> "6h"),
          TaskGroup(
            "description" -> "TaskGroup in TaskItem",
            "other_tasks" -> Seq(
              Task("description" -> "Task_431", "duration" -> "10h"),
            )
          )
        )
      )
    )
  )

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(task_management.asInstanceOf[FR.Group])) // form, data_handler
  }
}

