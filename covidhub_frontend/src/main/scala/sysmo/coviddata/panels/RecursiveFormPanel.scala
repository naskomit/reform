package sysmo.coviddata.panels

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.forms.{FormDataHandler, FormEditorComponent}
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{form => F}

import scala.concurrent.Future

object RecursiveFormPanel extends ApplicationPanel {

  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        FormEditorComponent(p.form, p.data_handler)
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("RecursiveFormPanel")
      .initialState(State())
      .renderBackend[Backend]
      .build

  val graph = MemGraph()

  val task_subform = F.FormGroup.builder(graph, "task")
      .field(_.char("name"))
      .field(_.select("responsible"))
      .field(_.int("duration"))
    .build

  val form: F.FormGroup =
    F.FormGroup.builder(graph, "recursive").descr("Recursive")
      .group("group", _.descr("Name")
        .field(_.char("name"))
      )
//      .group("t1", task_subform)
//      .group("t2", task_subform)
      .array("task", _.descr("Task")
        .field(_.char("name"))
        .field(_.select("responsible"))
        .field(_.int("duration")))
      .build

  val init_data: F.ValueMap = {
    F.ValueMap.builder
    .build
  }

  object data_handler extends FormDataHandler(graph) {
    override def initial_data: F.ValueMap = init_data
    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
      val choices = element.path match {
        case _ => Seq()
      }
      Future(choices)
    }
  }

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(form, data_handler))
  }
}

