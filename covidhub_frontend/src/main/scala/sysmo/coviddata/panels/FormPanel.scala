package sysmo.coviddata.panels

import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.forms.FormEditorComponent
import sysmo.reform.shared.form.runtime.FormRuntime
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.form.{build => FB}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.shared.form.build.GroupUnion


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

  def union(name: String, subtype1: FB.FieldGroup.Builder, other_subtypes: FB.FieldGroup.Builder*): GroupUnion.Builder =
    subtype1.union(name, x =>
      other_subtypes.foldLeft(x)(
        (acc: FB.GroupUnion.Builder, group: FB.FieldGroup.Builder) => acc | group
      )
    )

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(create()))
  }


}