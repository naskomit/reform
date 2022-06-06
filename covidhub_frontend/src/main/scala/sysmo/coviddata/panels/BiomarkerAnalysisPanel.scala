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
      .group("general", _.descr("General")
        .field(_.select("an_type").descr("Analysis type"))
        .field(_.char("analysis_name").descr("Analysis Name"))
      )
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
          _.value("bm_type", "qPCR").value("variable", "percent_change").value("biomarker", Seq("APOE", "PCDH1")),
          _.value("bm_type", "qPCR").value("variable", "fold_change").value("biomarker", Seq("APOE", "PCDH1")),
        )
      )
    .build
  }
  println(init_data)

  object data_handler extends FormDataHandler(graph) {
    override def initial_data: F.ValueMap = init_data
    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
      val analysis_types = Seq(
        LabeledValue("correlation", Some("Correlation analysis")),
        LabeledValue("DE", Some("Differential Expression analysis")),
        LabeledValue("PCA", Some("Principle component analysis")),
        LabeledValue("RCI", Some("RCI analysis")),
      )
      val biomarker_types = Seq(
        LabeledValue("blood_markers", Some("Blood markers")),
        LabeledValue("rna", Some("RNA")),
        LabeledValue("dna", Some("DNA")),
        LabeledValue("qPCR", Some("qPCR")),
      )
      val biomarker_variable = Seq(
        LabeledValue("raw_value", Some("Raw value")),
        LabeledValue("transformed_value", Some("Transformed value")),
        LabeledValue("fold_change", Some("Fold change")),
      )
      val transformation = Seq(
        LabeledValue("none", Some("None")),
        LabeledValue("log", Some("Log"))
      )
      val path = element.path
      logger.info(path.toString)
      val choices = path match {
        case F.PathMatch(Seq(_, "general", "an_type")) => analysis_types
        case F.PathMatch(Seq(_, "dep_var", "bm_type")) => biomarker_types
        case F.PathMatch(Seq(_, "dep_var", "variable")) => biomarker_variable
        case F.PathMatch(Seq(_, "dep_var", "transformation")) => transformation
        case F.PathMatch(Seq(_, "indep_var", _,  "bm_type")) => biomarker_types
        case F.PathMatch(Seq(_, "indep_var", _, "variable")) => biomarker_variable
        case F.PathMatch(Seq(_, "indep_var", _, "transformation")) => transformation
        case _ => Seq()
      }
      Future(choices)
    }
  }

  def apply(app_config: ApplicationConfiguration): Unmounted = {
    component(Props(form, data_handler))
  }
}

