//package sysmo.coviddata.panels
//
//import japgolly.scalajs.react.vdom.VdomElement
//import japgolly.scalajs.react.vdom.html_<^._
//import japgolly.scalajs.react._
//import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
//import sysmo.reform.ApplicationConfiguration
//import sysmo.reform.components.ApplicationPanel
//import sysmo.reform.components.forms.{FormDataHandler, FormEditorComponent}
//import sysmo.reform.shared.expr.{Expression => E}
//import sysmo.reform.shared.{form => F}
//import sysmo.reform.shared.gremlin.memg.MemGraph
//import sysmo.reform.shared.util.LabeledValue
//
//import scala.concurrent.Future
//
//object BiomarkerAnalysisPanel extends ApplicationPanel {
//
//  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
//  case class State()
//  final class Backend($: BackendScope[Props, State]) {
//    def render (p: Props, s: State): VdomElement = {
//      <.div(
//        FormEditorComponent(p.form, p.data_handler)
//      )
//    }
//  }
//
//  val component =
//    ScalaComponent.builder[Props]("BiomarkerAnalysisPanel")
//      .initialState(State())
//      .renderBackend[Backend]
//      .build
//
//  val graph = MemGraph()
//
//  val form: F.FormGroup = {
//    F.FormGroup.builder(graph, "biomarker_analysis").descr("Biomarker Analysis")
////      .group("general", _.descr("General")
////        .field(_.select("an_type").descr("Analysis type"))
////        .field(_.char("analysis_name").descr("Analysis Name"))
////      )
////      .group("dep_var", _.descr("Dependent Variable")
////        .field(_.select("bm_type").descr("Biomarker Type"))
////        .field(_.select("variable").descr("Variable"))
////        .field(_.select("transformation").descr("Transformation"))
////        .field(_.select("biomarker").descr("Biomarker").multiple())
////      )
////      .array("indep_var", _.descr("Independent Variables")
////        .field(_.select("bm_type").descr("Biomarker Type"))
////        .field(_.select("variable").descr("Variable"))
////        .field(_.select("transformation").descr("Transformation"))
////        .field(_.select("biomarker").descr("Biomarker").multiple())
////      )
//      .build
//  }
//
//  val init_data: F.ValueMap = {
//    F.ValueMap.builder
//      .record("biomarker_analysis", _
//        .record("dep_var", _
//          .value("bm_type", "qPCR").value("variable", "raw_value").value("biomarker", Seq("APOE", "PCDH1"))
//        )
//        .array("indep_var",
//          _.value("bm_type", "qPCR").value("variable", "raw_value").value("biomarker", Seq("APOE", "PCDH1")),
//          _.value("bm_type", "qPCR").value("variable", "percent_change").value("biomarker", Seq("APOE", "PCDH1")),
//          _.value("bm_type", "qPCR").value("variable", "fold_change").value("biomarker", Seq("APOE", "PCDH1")),
//        )
//      )
//    .build
//  }
//
//  object data_handler extends FormDataHandler(graph) {
//    override def initial_data: F.ValueMap = init_data
//    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
//      val analysis_types = Seq(
//        LabeledValue("correlation", Some("Correlation analysis")),
//        LabeledValue("DE", Some("Differential Expression analysis")),
//        LabeledValue("PCA", Some("Principle component analysis")),
//        LabeledValue("RCI", Some("RCI analysis")),
//      )
//      val biomarker_types = Seq(
//        LabeledValue("blood_markers", Some("Blood markers")),
//        LabeledValue("rna", Some("RNA")),
//        LabeledValue("dna", Some("DNA")),
//        LabeledValue("qPCR", Some("qPCR")),
//      )
//      val biomarker_variable = Seq(
//        LabeledValue("raw_value", Some("Raw value")),
//        LabeledValue("transformed_value", Some("Transformed value")),
//        LabeledValue("fold_change", Some("Fold change")),
//      )
//
//      val biomarkers = Seq("APOE", "PCDH1",  "ALDH1A1", "ALDH2", "MAO-A", "MAO-B")
//        .map(LabeledValue(_))
//
//      val transformation = Seq(
//        LabeledValue("none", Some("None")),
//        LabeledValue("log", Some("Log"))
//      )
//      val path = element.path
////      logger.info(path.toString)
//      val choices = path match {
//        case F.PathMatch(Seq(_, "general", "an_type")) => analysis_types
//        case F.PathMatch(Seq(_, "dep_var", "bm_type")) => biomarker_types
//        case F.PathMatch(Seq(_, "dep_var", "variable")) => biomarker_variable
//        case F.PathMatch(Seq(_, "dep_var", "biomarker")) => biomarkers
//        case F.PathMatch(Seq(_, "dep_var", "transformation")) => transformation
//        case F.PathMatch(Seq(_, "indep_var", _, "bm_type")) => biomarker_types
//        case F.PathMatch(Seq(_, "indep_var", _, "variable")) => biomarker_variable
//        case F.PathMatch(Seq(_, "indep_var", _, "transformation")) => transformation
//        case F.PathMatch(Seq(_, "indep_var", _, "biomarker")) => biomarkers
//        case _ => Seq()
//      }
//      Future(choices)
//    }
//  }
//
//  def apply(app_config: ApplicationConfiguration): Unmounted = {
//    component(Props(form, data_handler))
//  }
//}
//
