package sysmo.reform.shared.form.examples

import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.form.runtime.Group
import sysmo.reform.shared.form.{FormModelBuilder, FormModelHelper, build => FB, runtime => FR}


object Company extends FormModelHelper {
  class Builder extends FormModelBuilder {
    import FR.instantiation._
    import FR.FieldValue.implicits._

    override val name: String = "Company"
    override def create(build_graph: BuildGraph, runtime: Runtime): Group = {
      val CompanyBuilder = field_group("Company")
      val CompanyInfoBuilder = field_group(name = "CompanyInfo")
      val PersonBuilder = field_group("Person")
      val ProjectBuilder = field_group("Project")
      val DivisionBuilder = field_group("Division")
      val GroupBuilder = field_group("Group")
      val UnitBuilder = DivisionBuilder.union("Unit", _ | GroupBuilder)

      val CompanyInfo = CompanyInfoBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("address").descr("Address"), _.char)
        .field(_("city").descr("City"), _.char)
        .field(_("country").descr("Country"), _.char)
        .build

      val Person = PersonBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("age").descr("Age"), _.int)
        .field(_("cid").descr("Company ID"), _.char.unique())
        .build

      val Division = DivisionBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("symbol").descr("Symbol"), _.char)
        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
        .array(_("units").descr("Units"), UnitBuilder)
        .ref(_("people").descr("People"), _(PersonBuilder, "cid").label_expr(E.field("name")).multiple())
        .build

      val Group = GroupBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("symbol").descr("Symbol"), _.char)
        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
        .ref(_("people").descr("People"), _(PersonBuilder, "cid").label_expr(E.field("name")).multiple())
        .build

      val Project = ProjectBuilder
        .field(_("name").descr("Name"), _.char)
        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
        .build

      val Company = CompanyBuilder
        .group(_("info").descr("Info"), CompanyInfoBuilder)
        .array(_("people").descr("People"), PersonBuilder)
        .array(_("units").descr("Units"), UnitBuilder)
        .array(_("projects").descr("Projects"), ProjectBuilder)
        .layout("tabbed")
        .build

      val my_company = runtime.instantiate(
        Company(
          "info" -> CompanyInfo(
            "name" -> "The Corporation Inc.",
            "address" -> "123 Main str.",
            "city" -> "New Paris",
            "country" -> "New Europe",
          ),
          "people" -> Seq(
            Person("cid" -> "u101", "name" -> "John Doe"),
            Person("cid" -> "u201", "name" -> "Old Sam"),
            Person("cid" -> "u202", "name" -> "Young Sam"),
          ),
          "units" -> Seq(
            Division(
              "name" -> "Engineering", "symbol" -> "E",
              "manager" -> ref(E.field("cid") === "u101")
            ),
            Division("name" -> "Administration", "symbol" -> "A"),
            Division("name" -> "Purchase", "symbol" -> "P"),
            Division("name" -> "Marketing", "symbol" -> "M"),
          ),
          "projects" -> Seq(
            Project("name" -> "Clean the world")
          )
        )
      )
      my_company.asInstanceOf[FR.Group]
    }
  }
  def builder: Builder = new Builder
}
