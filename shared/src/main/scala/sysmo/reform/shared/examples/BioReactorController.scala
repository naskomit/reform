package sysmo.reform.shared.examples

import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.runtime.{RFObject, RFRuntime}
import sysmo.reform.shared.data.Value

object BioReactorController extends ModelBuilder {
  object type_builder extends TypeBuilder {
    val Parameter = record("Parameter")
      .label_expr(E.field("name")) +
      f_char("name")

    val Input = record("Input")
      .label_expr(E.field("name")) +
      f_char("name")

    val Output = record("Output")
      .label_expr(E.field("name")) +
      f_char("name")

//      .label_expr(E.field("name")) +
//      f_char("name")

    val HysteresisBlock = record("Hysteresis")
      .fields(
        f_char("setpoint"),
        f_char("delta"),
        f_char("active_when_high"),
      )

    val Block = union("Block", HysteresisBlock)

    val McNode = record("McNode")
      .label_expr(E.field("name"))
      .fields(
        f_char("name"),
        f_char("type"),
        f_real("tick_rate"),
        f_array("parameters", Parameter),
        f_array("inputs", Input),
        f_array("outputs", Output),
        f_array("blocks", Block),
      )
  }

  class initializer1[F[+X]](runtime: RFRuntime[F])
    extends Initializer(runtime) {
    import Value.implicits._
    import inst._
    import type_builder._
    val root: F[RFObject[F]] =
      inst(
        McNode(
          "name" -> "ESP32-1",
          "type" -> "ESP32",
          "tick_rate" -> 1000,
          "parameters" -> Seq(
            Parameter("name" -> "t_target"),
            Parameter("name" -> "dt"),
            Parameter("name" -> "t_mixer_on"),
            Parameter("name" -> "t_mixer_off"),
            Parameter("name" -> "gas_cyl_min"),
            Parameter("name" -> "gas_cyl_max"),
            Parameter("name" -> "ph_min"),
            Parameter("name" -> "ph_max"),
            Parameter("name" -> "ph_target"),
          ),
          "inputs" -> Seq(
            Input("name" -> "reactor_t"),
            Input("name" -> "reactor_p"),
            Input("name" -> "reactor_ph"),
            Input("name" -> "gas_cyl_pos"),
          ),
          "outputs" -> Seq(
            Output("name" -> "heater_on"),
            Output("name" -> "mixer_rate"),
            Output("name" -> "acid_on"),
            Output("name" -> "base_on"),
            Output("name" -> "gas_release"),
          ),
          "blocks"-> Seq()
        )
      )
  }

  object initializer1 {
    def apply[F[+_]](runtime: RFRuntime[F]) =
      new initializer1[F](runtime)
  }
}
