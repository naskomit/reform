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
import sysmo.reform.components.layouts.{NamedContent, TabbedLayout}
import japgolly.scalajs.react.vdom.html_<^._

import scala.concurrent.Future

object RefrigerationCyclePanel extends ApplicationPanel {
  import japgolly.scalajs.react._

  case class Props(form: F.FormGroup, data_handler: FormDataHandler)
  case class State()
  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(
        TabbedLayout(Seq(
          NamedContent("Tab 1", <.div("The whole of life is just like watching a film. Only it’s as though you always get in ten minutes after the big picture has started, and no-one will tell you the plot, so you have to work it out all yourself from the clues. —from Moving Pictures")),
          NamedContent("Tab 2", <.div("Real stupidity beats artificial intelligence every time. —from Hogfather")),
          NamedContent("Tab 3", <.div("It’s not worth doing something unless someone, somewhere, would much rather you weren’t doing it. —from the foreword to The Ultimate Encyclopedia of Fantasy, by David Pringle")),
          NamedContent("Tab 4", <.div("There are times in life when people must know when not to let go. Balloons are designed to teach small children this.")),
        )),
        FormEditorComponent(p.form, p.data_handler)
      )
    }
  }

  val component =
    ScalaComponent.builder[Props]("AnalysisPanel")
      .initialState(State())
      .renderBackend[Backend]
//      .componentDidMount(f => f.backend.init(f.props))
      .build

  val graph = MemGraph()
  val refrigeration_form: F.FormGroup = {
    import F.FieldValue.implicits._
    F.FormGroup.builder(graph, "refrigeration_cycle").descr("Refrigeration cycle")
      .field(_.char("aname").descr("Analysis Name"))
      .field(_.int("n_cycles").descr("Number of cycles"))
      .field(_.bool("save").descr("Save analysis"))
      .group("cycle_definition", _.descr("Cycle definition")
        .group("cycle_params", _.descr("Cycle Parameters")
          .field(_.select("fluid").descr("Working fluid"))
          .field(_.float("flow_rate").descr("Fluid flow rate"))
          .field(_.select("warm_by").descr("Warm side defined by"))
          .field(_.float("p_warm").descr("Warm side pressure").show(E.field("warm_by") === "p"))
          .field(_.float("T_warm").descr("Warm side temperature").show(E.field("warm_by") === "T"))
          .field(_.select("cold_by").descr("Cold side defined by"))
          .field(_.float("p_cold").descr("Cold side pressure").show(E.field("cold_by") === "p"))
          .field(_.float("T_cold").descr("Cold side temperature").show(E.field("cold_by") === "T"))
        ).group("compressor", _.descr("Compressor")
          .field(_.select("model"))
          .field(_.float("isentropic_efficiency").show(E.field(("model")) === "isentropic"))
          .field(_.float("heat_loss_factor").show(E.field(("model")) === "isentropic"))
          .field(_.float("isosthermal_efficiency").show(E.field(("model")) === "isothermal"))
          .field(_.float("temperature_increase").show(E.field(("model")) === "isothermal"))
        ).group("condenser", _.descr("Condenser")
          .field(_.select("outlet_by"))
          .field(_.float("pressure").show(E.field("outlet_by") === "p"))
          .field(_.float("temperature").show(E.field("outlet_by") === "T"))
        ).group("evaporator", _.descr("Evaporator")
          .field(_.select("outlet_by"))
          .field(_.float("pressure").show(E.field("outlet_by") === "p"))
          .field(_.float("temperature").show(E.field("outlet_by") === "T"))
        )
      ).group("diagram_settings", _.descr("Diagram settings")
        .field(_.bool("create_process_diagram"))
        .field(_.bool("isotherms"))
        .field(_.bool("isochorees"))
        .field(_.bool("isentrops"))
      ).group("solver", _.descr("Solver")
      ).group("result", _.descr("Results")

      ).build
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
        //.record("compressor", _.)
      )
      .build
  }

  object refrigeration_data_handler extends FormDataHandler(graph) {
    override def initial_data: F.ValueMap = refrigeration_data_init
    override def get_choices(element: F.FormElement): Future[Seq[LabeledValue[_]]] = {
      val path = element.path
      val thermodynamic_state_choices = Seq(
        LabeledValue("p", Some("Pressure")),
        LabeledValue("T", Some("Temperature"))
      )
      logger.info(path.toString)
      val choices = path.segments match {
        case Seq(_, "cycle_definition", "cycle_params", "fluid") => Seq("para-Hydrogen", "orho-Hydrogen", "water", "R134a").map(x => LabeledValue(x))
        case Seq(_, "cycle_definition", "cycle_params", "warm_by") => thermodynamic_state_choices
        case Seq(_, "cycle_definition", "cycle_params", "cold_by") => thermodynamic_state_choices
        case Seq(_, "cycle_definition", "condenser", "outlet_by") => thermodynamic_state_choices
        case Seq(_, "cycle_definition", "evaporator", "outlet_by") => thermodynamic_state_choices
        case Seq(_, "cycle_definition", "compressor", "model") => Seq(
          LabeledValue("isentropic"),
          LabeledValue("isothermal"),
        )
        case _ => Seq()
      }
      Future(choices)
    }

  }

  def apply(app_config: ApplicationConfiguration): Unmounted = {

    component(Props(refrigeration_form, refrigeration_data_handler))
  }
}
