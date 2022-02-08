package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.components.editors.StringEditor
import sysmo.coviddata.components.forms.{StreamingFormEditor}
import sysmo.coviddata.data.{DefaultAsyncDataInterface, StreamingRecordManager}
import sysmo.coviddata.shared.data.PatientRecord

object Form1Panel {
  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      StreamingFormEditor(
        StreamingRecordManager(
          PatientRecord(
            first_name = "Джон", father_name = "И", last_name = "Атанасов",
            age = 63, gender = "мъж", education = "висше"
          )
        ), "aaa"
      )
//      FormEditor(DefaultAsyncDataInterface, "aaa")
//      <.div(
//
//      )
//        "Form1",
//        StringEditor("str_ed", "Form1", "String Editor", "Hello"))

    }
  }

  val component = ScalaComponent.builder[Props]("Form1")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component(Props())
}
