package sysmo.reform.shared.examples

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.examples.SkullInventoryBuilder.type_builder
import sysmo.reform.shared.types.RecordType
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.runtime.{RFObject, RFRuntime, RuntimeConstructor}

object MicroController extends ModelBuilder {
  object type_builder extends TypeSystemBuilder {
    def io_common(gb: RecordType.Builder): RecordType.Builder =
      gb.label_expr(E.field("name")) +
        f_char("name") +
        f_char("descr") +
        f_int("pin")
    //      .keys(Seq("name"))

    /** Data structures */
    val PrimitiveType = record("PrimitiveType")
      .label_expr(E.field("symbol")) +
      f_char("symbol")

    val Field = record("Field")
    val StructureType = record("Structure")
      .label_expr(E.field("symbol")) +
      f_char("symbol") +
      f_array("fields", Field)

    val Type = union("Type", PrimitiveType, StructureType)

    Field + f_char("name") +
      f_char("descr")
    //+ f_ref("type", Type)

    /** Input - Output */
    val DigitalInput = record("DigitalInput")
      .descr("Input with 'high' and 'low' states")
      .extend(io_common)

    val AnalogInput = record("AnalogInput")
      .extend(io_common) +
      f_char("quantity")

    val Input = union("Input", DigitalInput, AnalogInput)


    val DigitalOutput = record("DigitalOutput")
      .extend(io_common)

    val PWMOutput = record("PWMOutput")
      .extend(io_common)

    val Output = union("Output", DigitalOutput, PWMOutput)

    val Trigger = record("Trigger").label_expr(E.field("name")) +
      f_char("name")

    //    val Event = union("Event", InputEvent)

    val Controller = record("Controller") +
      f_array("types", Type) +
      f_array("inputs", Input).label_expr(E.field("name")) +
      f_array("outputs", Output).label_expr(E.field("name")) +
//      f_ref("state", Type) +
      f_array("triggers", Trigger)
  }

  class initializer1[F[+X]](runtime_builder: RuntimeConstructor[F])
    extends Initializer(runtime_builder) {
    import Value.implicits._
    import inst._
    import type_builder._
    val root: F[RFObject[F]] =
      inst(
        Controller(
          "types" -> Seq(
            PrimitiveType("symbol" -> "Integer"),
            PrimitiveType("symbol" -> "Float"),
            PrimitiveType("symbol" -> "Boolean"),
            StructureType(
              "symbol" -> "TCState",
              "fields" -> Seq(
                Field("name" -> "TSet", "descr" -> "Setpoint temperature"),
                Field("name" -> "dT", "descr" -> "Delta T")
              ))
          ),
          "inputs" -> Seq(
            AnalogInput(
              "name" -> "Tamb", "descr" -> "Ambient temperature",
              "quantity" -> "Temperature", "pin" -> 12
            )
          ),
          "outputs"-> Seq(
            DigitalOutput(
              "name" -> "heat_on", "descr" -> "Heater On",
              "pin" -> 14
            )
          ),
          "triggers" -> Seq(
            Trigger("name" -> "Temperature too high")
          )
        )
      )
    }

  object initializer1 {
    def apply[F[+_]](runtime_constructor: RuntimeConstructor[F]) =
      new initializer1[F](runtime_constructor)
  }
}
