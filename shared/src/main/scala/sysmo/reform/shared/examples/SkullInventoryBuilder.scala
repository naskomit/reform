package sysmo.reform.shared.examples

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.runtime.{RFObject, RFRuntime, RuntimeConstructor}

object SkullInventoryBuilder extends ModelBuilder {
  object type_builder extends TypeBuilder {
    /** Data structures */
    val SkullSample = record("SkullEntry").label_expr(E.field("code")) +
      f_char("code") +
      f_char("image_type") +
      f_char("sex") +
      f_int("age") +
      f_int("stature") +
      f_real("BMI") +
      f_char("BMI category") +
      f_char("Filter 1") +
      f_char("Filter 2")

    val SkullInventory = record("SkullInventory") +
      f_array("samples", SkullSample)

  }

  class initializer1[F[+_]](runtime_builder: RuntimeConstructor[F])
    extends Initializer(runtime_builder, type_builder.build) {
    import Value.implicits._
    import inst._
    import type_builder._
    val root: F[RFObject[F]] =
      inst(SkullInventory(
        "samples" -> Seq(
          SkullSample("code" -> "M0.1.")
        )
      ))
  }

  object initializer1 {
    def apply[F[+_]](runtime_constructor: RuntimeConstructor[F]) =
      new initializer1[F](runtime_constructor)
  }
}
