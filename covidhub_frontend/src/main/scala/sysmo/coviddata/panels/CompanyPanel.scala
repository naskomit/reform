package sysmo.coviddata.panels

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.components.forms.FormEditorComponent
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.shared.form.runtime.FormRuntime
import sysmo.reform.shared.expr.{Expression => E}

trait FormPanel extends ApplicationPanel {
  case class Props(group: FR.Group)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      FormEditorComponent(p.group)
    }
  }

  val display_name: String
  def create(): FR.Group

  val component =
    ScalaComponent.builder[Props](display_name)
      .initialState(State())
      .renderBackend[Backend]
      .build

  val build_graph: MemGraph = MemGraph()
  val runtime: FormRuntime = FR.FormRuntime(build_graph)
  def field_group(name: String): FB.FieldGroup.Builder =
    FB.FieldGroup.builder(build_graph, name)

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(create()))
  }


}

object CompanyPanel extends FormPanel {
  import FR.instantiation._
  import FR.FieldValue.implicits._

  override val display_name: String = "CompanyPanel"
  def create(): FR.Group = {

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
      .build

    val Group = GroupBuilder
      .field(_("name").descr("Name"), _.char)
      .field(_("symbol").descr("Symbol"), _.char)
//      .ref(_("manager").descr("Manager"), PersonBuilder)
//      .ref_array(_("people").descr("People"), PersonBuilder)
      .build

    val Project = ProjectBuilder
      .field(_("name").descr("Name"), _.char)
//      .ref(_("manager").descr("Manager"), PersonBuilder)
      .build

    val Company = CompanyBuilder
      .group(_("info").descr("Info"), CompanyInfoBuilder)
      .array(_("people").descr("People"), PersonBuilder)
      .array(_("units").descr("Units"), UnitBuilder)
      .array(_("projects").descr("Projects"), ProjectBuilder)
//      .layout("tabbed")
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