package sysmo.reform.shared.form.examples

import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.form.runtime.Group
import sysmo.reform.shared.form.{FormModelBuilder, FormModelHelper, runtime => FR}


object TaskManagement extends FormModelHelper {
  class Builder extends FormModelBuilder {
    import FR.FieldValue.implicits._
    import FR.instantiation._

    override val name: String = "TaskManagement"
    override def create(build_graph: BuildGraph, runtime: Runtime): Group = {
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
      task_management.asInstanceOf[FR.Group]
    }
  }
  def builder: Builder = new Builder
}
