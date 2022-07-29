package sysmo.coviddata.panels

import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.forms.{FormEditorComponent, TreeBrowser}
import sysmo.reform.shared.form.{FormModelBuilder, build => FB, runtime => FR}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._


trait FormPanel extends ApplicationPanel {
  case class Props(group: FR.Group)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      //FormEditorComponent(p.group)
      TreeBrowser(p.group.runtime, p.group.id)
    }
  }


  val fmb: FormModelBuilder

  val component =
    ScalaComponent.builder[Props](fmb.name)
      .initialState(State())
      .renderBackend[Backend]
      .build


  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(fmb.build()))
  }


}