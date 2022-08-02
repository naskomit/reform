package sysmo.reform.shared.form.examples

import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.form.runtime.Group
import sysmo.reform.shared.form.{FormModelBuilder, FormModelHelper, build => FB, runtime => FR}

object MicroController extends FormModelHelper {
  class Builder extends FormModelBuilder {

    import FR.instantiation._

    override val name: String = "MicroController"
    override def create(build_graph: BuildGraph, runtime: Runtime): Group = {

      def io_common(gb: FB.FieldGroup.Builder): FB.FieldGroup.Builder = gb
        .field(_("name"), _.char)
        .field(_("descr"), _.char)
        .field(_("pin"), _.int)
        .keys(Seq("name"))
        .label_expr(E.field("name"))

      val DigitalInput = field_group("DigitalInput").extend(io_common)
      val AnalogInput = field_group("AnalogInput").extend(io_common)
        .field(_("quantity"), _.char)

      val DigitalOutput = field_group("DigitalOutput").extend(io_common)
      val AnalogOutput = field_group("AnalogOutput").extend(io_common)
        .field(_("quantity"), _.char)

      val PWMOutput = field_group("PWMOutput").extend(io_common)
      val Input = union("Input", DigitalInput, AnalogInput)
      val Output = union(name = "Output", DigitalOutput, AnalogOutput, PWMOutput)

      val Field = field_group("Field")
      val Structure = field_group("Structure")
        .field(_("symbol").descr("Symbol"), _.char)
        .array(_("fields").descr("Fields"), Field)
        .label_expr(E.field("name"))

//      val VariableType = union("VariableType", IntegerVariable, FloatVariable, BooleanVariable, Structure)

      Field
        .field(_("name").descr("Name"), _.char)
        .field(_("descr").descr("Description"), _.char)
        .ref(_("type").descr("Type"), _(Structure, "symbol").label_expr(E.field("symbol")))

      val Controller = field_group("Controller")
        .array(_("structures").descr("Structures"), Structure, _.label_expr(E.field("symbol")))
        .array(_("inputs").descr("Inputs"), Input)
        .array(_("outputs").descr("Outputs"), Output)
        .ref(_("state").descr("State"), _(Structure, "symbol").label_expr(E.field("symbol")))

      val controller = runtime.instantiate(
        Controller(
          "structures" -> Seq(
            Structure("symbol" -> "Integer"),
            Structure("symbol" -> "Float"),
            Structure("symbol" -> "Boolean"),
            Structure("symbol" -> "TCState", "fields" -> Seq(
              Field("name" -> "TSet", "descr" -> "Setpoint temperature"),
              Field("name" -> "dT", "descr" -> "Delta T")
            ))
          ),
          "inputs" -> Seq(
            AnalogInput.apply(
              "name" -> "Tamb", "descr" -> "Ambient temperature",
              "quantity" -> "Temperature", "pin" -> 12
            )
          ),
          "outputs"-> Seq(
            DigitalOutput(
              "name" -> "heat_on", "descr" -> "Heater On",
              "pin" -> 14
            )
          )
        )
      )
      controller.asInstanceOf[FR.Group]
    }
  }
  def builder: Builder = new Builder
}
