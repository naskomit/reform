package sysmo.coviddata.panels

import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.expr.{Expression => E}

object ReactionKineticsPanel extends FormPanel {
  import FR.instantiation._
  import FR.FieldValue.implicits._

  override val display_name: String = "ReactionKineticsPanel"
  def create(): FR.Group = {

    val SpeciesBuilder = field_group("Species")

    val ForwardReactionBuilder = field_group(name = "ForwardReaction")
    val ReversibleReactionBuilder = field_group("ReversibleReaction")

    val ReactionBuilder = union("Reaction", ForwardReactionBuilder, ReversibleReactionBuilder)

    val SolverBuilder = field_group("Solver")
    val TimePlotBuilder = field_group("TimePlot")
    val PhasePlotBuilder = field_group("PhasePlot")

    val PlotBuilder = union("Plot", TimePlotBuilder, PhasePlotBuilder)
    val SimulationBuilder = field_group("Simulation")


    val Specie = SpeciesBuilder
      .field(_("name").descr("Name"), _.char)
      .field(_("symbol").descr("Symbol"), _.char)
      .build

    val sp_ref = (x: FB.Reference.BuilderSource) => x(SpeciesBuilder, "symbol").label_expr(E.field("symbol"))

    val ForwardReaction = ForwardReactionBuilder
      .field(_("name").descr("Name"), _.char)
      .field(_("k_f").descr("Forward rate"), _.float)
      .ref(_("r1").descr("Reactant 1"), sp_ref)
      .ref(_("r2").descr("Reactant 2"), sp_ref)
      .ref(_("p").descr("Product"), sp_ref)
      .build

    val ReversibleReaction = ReversibleReactionBuilder
      .field(_("name").descr("Name"), _.char)
      .field(_("k_f").descr("Forward rate"), _.float)
      .field(_("k_b").descr("Reverse rate"), _.float)
      .ref(_("r1").descr("Reactant 1"), sp_ref)
      .ref(_("r2").descr("Reactant 2"), sp_ref)
      .ref(_("p").descr("Product"), sp_ref)
      .build

    val Solver = SolverBuilder
      .field(_("t_sim").descr("Simulation time"), _.float)
      .field(_("t_print").descr("Print interval"), _.float)
      .build


    val PhasePlot = PhasePlotBuilder
      .ref(_("x_var").descr("X variable"), sp_ref)
      .ref(_("y_var").descr("Y variable"), sp_ref)
      .build

    val Simulation = SimulationBuilder
      .array(_("species").descr("Species"), SpeciesBuilder)
      .array(_("reactions").descr("Reactions"), ReactionBuilder)
      .group(_("solver").descr("Solver"), SolverBuilder)
      .array(_("plots").descr("Plots"), PlotBuilder)
      .layout("tabbed")
      .build

    val my_company = runtime.instantiate(
      Simulation(
        "species" -> Seq(
          Specie("name" -> "Substrate", "symbol" -> "S"),
          Specie("name" -> "Enzyme", "symbol" -> "E"),
          Specie("name" -> "Product", "symbol" -> "P"),
        )
      )
    )
    my_company.asInstanceOf[FR.Group]
  }

}