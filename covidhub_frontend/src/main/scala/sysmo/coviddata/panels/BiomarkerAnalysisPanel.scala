package sysmo.coviddata.panels

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.ApplicationConfiguration
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.components.forms4.{FormDataHandler, FormEditorComponent}
import sysmo.reform.shared.data.{form4 => F}
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.util.LabeledValue
import scala.concurrent.Future

object BiomarkerAnalysisPanel extends ApplicationPanel {

  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        //        TabbedLayout(Seq(
        //          NamedContent("Tab 1", <.div("The whole of life is just like watching a film. Only it’s as though you always get in ten minutes after the big picture has started, and no-one will tell you the plot, so you have to work it out all yourself from the clues. —from Moving Pictures")),
        //          NamedContent("Tab 2", <.div("Real stupidity beats artificial intelligence every time. —from Hogfather")),
        //          NamedContent("Tab 3", <.div("It’s not worth doing something unless someone, somewhere, would much rather you weren’t doing it. —from the foreword to The Ultimate Encyclopedia of Fantasy, by David Pringle")),
        //          NamedContent("Tab 4", <.div("There are times in life when people must know when not to let go. Balloons are designed to teach small children this.")),
        //        )),
        FormEditorComponent(p.form, p.data_handler)
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("BiomarkerAnalysisPanel")
      .initialState(State())
      .renderBackend[Backend]
      .build

  val graph = MemGraph()

  val form: F.FormGroup = {
    import F.FieldValue.implicits._
    F.FormGroup.builder(graph, "biomarker_analysis").descr("Biomarker Analysis")
      .field(_.select("general").descr("General"))
      .field(_.char("analysis_name").descr("Analysis Name"))
      .group("dep_var", _.descr("Dependent Variable")
        .field(_.select("bm_type").descr("Biomarker Type"))
        .field(_.select("variable").descr("Variable"))
        .field(_.select("transformation").descr("Transformation"))
        .field(_.select("biomarker").descr("Biomarker").multiple())
      )
      .array("indep_var", _.descr("Independent Variables")
        .field(_.select("bm_type").descr("Biomarker Type"))
        .field(_.select("variable").descr("Variable"))
        .field(_.select("transformation").descr("Transformation"))
        .field(_.select("biomarker").descr("Biomarker").multiple())
      )
      .build
  }

  val init_data: F.ValueMap = {
    F.ValueMap.builder
      .record("biomarker_analysis", _
        .record("dep_var", _
          .value("bm_type", "qPCR")
          .value("variable", "raw_value")
          .value("biomarker", Seq("APOE", "PCDH1"))
        )
        .array("indep_var",
          _.value("bm_type", "qPCR").value("variable", "raw_value").value("biomarker", Seq("APOE", "PCDH1")),
          _.value("bm_type", "qPCR").value("variable", "raw_value").value("biomarker", Seq("APOE", "PCDH1")),
          _.value("bm_type", "qPCR").value("variable", "raw_value").value("biomarker", Seq("APOE", "PCDH1")),
        )
      )
    .build
  }
  println(init_data)

  object data_handler extends FormDataHandler(graph) {
    override def initial_data: F.ValueMap = init_data
    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
      val path = element.path
      logger.info(path.toString)
      val choices = path.segments match {
        case _ => Seq()
      }
      Future(choices)
    }
  }

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(form, data_handler))
  }
}


//
//object AnalysisPanel extends ApplicationPanel {
//  import japgolly.scalajs.react._
//
//  case class Props(form: F.Form, data_handler: FormDataHandler)
//  case class State()
//  final class Backend($: BackendScope[Props, State]) {
//    def render (p: Props, s: State): VdomElement = FormEditorComponent(p.form, p.data_handler)
//  }
//
//  val component =
//    ScalaComponent.builder[Props]("AnalysisPanel")
//      .initialState(State())
//      .renderBackend[Backend]
////      .componentDidMount(f => f.backend.init(f.props))
//      .build
//
////  val analysis_definition_form: F.Form = {
////    F.Form.builder("analysis_definition")
////      .field(_.select("general").label("General"))
////      .field(_.char("analysis_name").label("Analysis Name"))
////      .group(_.fieldset("dep_var").label("Dependent Variable")
////        .field(_.select("bm_type").label("Biomarker Type"))
////        .field(_.select("variable").label("Variable"))
////        .field(_.select("panel").label("Panel"))
////        .field(_.select("transformation").label("Transformation"))
////        .field(_.select("biomarker").label("Biomarker"))
////        .field(_.bool("scale").label("Scale"))
////      )
////      .group(_.fieldset("indep_var").label("Independent Variable"))
////      .label("Analysis Definition")
////      .build
////  }
//
//  val refrigeration_form: F.Form = {
//    F.Form.builder("refrigeration_cycle").label("Refrigeration cycle")
//      .field(_.char("aname").label("Analysis Name"))
//      .field(_.int("n_cycles").label("Number of cycles"))
//      .field(_.bool("save").label("Save analysis"))
//      .group(_.fieldset("cycle_params").label("Cycle Parameters")
//        .field(_.select("fluid").label("Working fluid"))
//        .field(_.float("flow_rate").label("Fluid flow rate"))
//        .field(_.select("warm_by").label("Warm side defined by"))
//        .field(_.float("p_warm").label("Warm side pressure"))
//        .field(_.float("T_warm").label("Warm side temperature"))
//        .field(_.select("cold_by").label("Cold side defined by"))
//        .field(_.float("p_cold").label("Cold side pressure"))
//        .field(_.float("T_cold").label("Cold side temperature"))
//      )
//      .build
//  }
//
//  val refrigeration_data_init: FD.ValueMap = {
//    import FD._
//    ValueMap.builder
//      .value("aname", "Cycle 1")
//      .value("n_cycles", 10)
//      .value("save", true)
//      .record("cycle_params", _
//        .value("fluid", "R134a")
//        .value("flow_rate", 1.5)
//        .value("warm_by", "p")
//        .value("p_warm", 1e6)
//        .value("T_warm", 313)
//        .value("cold_by", "T")
//        .value("p_cold", 1e5)
//        .value("T_cold", 250)
//      )
//      .build
////      "cycle_params" -> ValueMap.fv(
////        "fluid" -> "R134a".fv,
////        "flow_rate" -> 1.5.fv
////      )
////    )
//  }
//
//  val refrigeration_data_handler: FormDataHandler = new FormDataHandler {
//    override val initial_data: FD.ValueMap = refrigeration_data_init
//    override def get_choices(path: F.ElementPath, data: FD.ValueMap): Future[Seq[LabeledValue[_]]] = path match {
//      case Seq("cycle_params", "fluid") => Future(Seq("para-Hydrogen", "orho-Hydrogen", "water", "R134a").map(x => LabeledValue(x)))
//    }
//  }
//
//  def apply(app_config: ApplicationConfiguration): Unmounted = {
//
//    component(Props(refrigeration_form, refrigeration_data_handler))
//  }
//}
