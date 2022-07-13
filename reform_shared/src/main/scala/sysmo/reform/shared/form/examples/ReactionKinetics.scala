package sysmo.reform.shared.form.examples

import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.expr.{Expression => E}

import sysmo.reform.shared.form.{FormModelBuilder, FormModelHelper}
import sysmo.reform.shared.form.runtime.{Group}

object ReactionKinetics extends FormModelHelper {
  class Builder extends FormModelBuilder {
    import FR.instantiation._
    import FR.FieldValue.implicits._

    override val name: String = "ReactionKinetics"
    override def create(build_graph: BuildGraph, runtime: Runtime): Group = {

      val SpeciesBuilder = field_group("Species")

      val ForwardReactionBuilder = field_group(name = "ForwardReaction")
      val ReversibleReactionBuilder = field_group("ReversibleReaction")

      val ReactionBuilder = union("Reaction", ForwardReactionBuilder, ReversibleReactionBuilder)
      val ReactionEquationBuilder = field_group("ReactionEquation")
      val EquationTermBuilder = field_group("EquationTerm")

      val SolverBuilder = field_group("Solver")
      val TimePlotBuilder = field_group("TimePlot")
      val PhasePlotBuilder = field_group("PhasePlot")

      val PlotBuilder = union("Plot", TimePlotBuilder, PhasePlotBuilder)
      val SimulationBuilder = field_group("Simulation")


      val Species = SpeciesBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("symbol").descr("Symbol"), _.char)
        .build

      val sp_ref = (x: FB.Reference.BuilderSource) => x(SpeciesBuilder, "symbol").label_expr(E.field("symbol"))

      val EquationTerm = EquationTermBuilder
        .ref(_("specise").descr("Species"), sp_ref)
        .field(_("quantity").descr("Quantity"), _.float)
        .build

      val ReactionEquation = ReactionEquationBuilder
        .array(_("reactants").descr("Reactants"), EquationTermBuilder)
        .array(_("products").descr("Products"), EquationTermBuilder)
        .build

      val ForwardReaction = ForwardReactionBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("k_f").descr("Rate"), _.float)
        .group(_("eq").descr("Equation"), ReactionEquationBuilder)
        .build

      val ReversibleReaction = ReversibleReactionBuilder
        .field(_("name").descr("Name"), _.char)
        .field(_("k_f").descr("Forward rate"), _.float)
        .field(_("k_b").descr("Reverse rate"), _.float)
        .group(_("eq").descr("Equation"), ReactionEquationBuilder)
        .build

      val Solver = SolverBuilder
        .field(_("t_sim").descr("Simulation time"), _.float)
        .field(_("t_print").descr("Print interval"), _.float)
        .build

      val TimePlot = TimePlotBuilder
        .ref(_("species").descr("Species"), x => sp_ref(x).multiple())
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

      val simulation = runtime.instantiate(
        Simulation(
          "species" -> Seq(
            Species("name" -> "Substrate", "symbol" -> "S"),
            Species("name" -> "Enzyme", "symbol" -> "E"),
            Species("name" -> "Product", "symbol" -> "P"),
          )
        )
      )
      simulation.asInstanceOf[FR.Group]
    }
  }
  def builder: Builder = new Builder
}
