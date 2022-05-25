package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.components.forms4.{FormDataHandler, FormEditorComponent}

import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import scala.concurrent.Future

object AnalysisPanel2 extends ApplicationPanel {
  import japgolly.scalajs.react._

  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = FormEditorComponent(p.form, p.data_handler)
  }

  val component =
    ScalaComponent.builder[Props]("AnalysisPanel")
      .initialState(State())
      .renderBackend[Backend]
//      .componentDidMount(f => f.backend.init(f.props))
      .build

//  val analysis_definition_form: F.Form = {
//    F.Form.builder("analysis_definition")
//      .field(_.select("general").label("General"))
//      .field(_.char("analysis_name").label("Analysis Name"))
//      .group(_.fieldset("dep_var").label("Dependent Variable")
//        .field(_.select("bm_type").label("Biomarker Type"))
//        .field(_.select("variable").label("Variable"))
//        .field(_.select("panel").label("Panel"))
//        .field(_.select("transformation").label("Transformation"))
//        .field(_.select("biomarker").label("Biomarker"))
//        .field(_.bool("scale").label("Scale"))
//      )
//      .group(_.fieldset("indep_var").label("Independent Variable"))
//      .label("Analysis Definition")
//      .build
//  }

  val graph = MemGraph()
  val refrigeration_form: F.FormGroup = {
    F.FormGroup.builder(graph, "refrigeration_cycle").descr("Refrigeration cycle")
      .field(_.char("aname").descr("Analysis Name"))
//      .field(_.int("n_cycles").descr("Number of cycles"))
//      .field(_.bool("save").descr("Save analysis"))
      .group("cycle_params", _.descr("Cycle Parameters")
        .field(_.select("fluid").descr("Working fluid"))
        .field(_.float("flow_rate").descr("Fluid flow rate"))
        .field(_.select("warm_by").descr("Warm side defined by"))
        .field(_.float("p_warm").descr("Warm side pressure").show(E.field("warm_by") === E(F.FieldValue("p"))))
        .field(_.float("T_warm").descr("Warm side temperature").show(E.field("warm_by") === E(F.FieldValue("T"))))
        .field(_.select("cold_by").descr("Cold side defined by"))
        .field(_.float("p_cold").descr("Cold side pressure"))
        .field(_.float("T_cold").descr("Cold side temperature"))
      )
      .build
  }

  val refrigeration_data_init: F.ValueMap = {
    F.ValueMap.builder
      .record("refrigeration_cycle", _
        .value("aname", "Cycle 1")
        .value("n_cycles", 10)
        .value("save", true)
        .record("cycle_params", _
          .value("fluid", "R134a")
          .value("flow_rate", 1.5)
          .value("warm_by", "p")
          .value("p_warm", 1e6)
          .value("T_warm", 313)
          .value("cold_by", "T")
          .value("p_cold", 1e5)
          .value("T_cold", 250)
        )
      )
      .build
//      "cycle_params" -> ValueMap.fv(
//        "fluid" -> "R134a".fv,
//        "flow_rate" -> 1.5.fv
//      )
//    )
  }

  object refrigeration_data_handler extends FormDataHandler(graph) {
    override def initial_data: F.ValueMap = refrigeration_data_init
    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
      val path = element.path
//      logger.info(path.toString)
      path.segments match {
        case Seq(_, "cycle_params", "fluid") => Future(Seq("para-Hydrogen", "orho-Hydrogen", "water", "R134a").map(x => LabeledValue(x)))
        case Seq(_, "cycle_params", "warm_by") => Future(Seq(
          LabeledValue("p", Some("Pressure")),
          LabeledValue("T", Some("Temperature"))
        ))
        case Seq(_, "cycle_params", "cold_by") => Future(Seq(
          LabeledValue("p", Some("Pressure")),
          LabeledValue("T", Some("Temperature"))
        ))
        case _ => Future(Seq())
      }
    }

  }

  def apply(app_config: ApplicationConfiguration): Unmounted = {

    component(Props(refrigeration_form, refrigeration_data_handler))
  }
}
