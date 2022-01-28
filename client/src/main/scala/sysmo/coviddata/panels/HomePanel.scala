package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.table.{AgGridColumn, AgGridComponent, DemoDataSource, DemoServerDataSource}

import scala.scalajs.js

object HomePanel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(^.style:=js.Dictionary("marginLeft" -> "100px", "marginRight" -> "100px"),
        AgGridComponent(DemoServerDataSource,
          Seq(
            AgGridColumn("first_name", "Име"),
            AgGridColumn("father_name", "Презиме"),
            AgGridColumn("last_name", "Фамилия"),
            AgGridColumn("age", "Възраст"),
            AgGridColumn("gender", "Пол"),
            AgGridColumn("education", "Образование"),
            AgGridColumn("contact_name", "Вероятен контакт"),
            AgGridColumn("contact_type", "Вид контакт"),
            AgGridColumn("contact_time", "Дни от контакта"),
            AgGridColumn("social_status", "Социален статус"),
            AgGridColumn("has_concomitant_diseases", "Наличие на придружаващи заболявания"),
            AgGridColumn("concomitant_diseases", "Вид придружаващо заболяване"),
            AgGridColumn("risk_factors", "Рискови фактори"),
            AgGridColumn("alcohol", "Алкохол"),
            AgGridColumn("other_risk_1", "Други 1"),
            AgGridColumn("other_risk_2", "Други 2"),
          ))
      )

    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
