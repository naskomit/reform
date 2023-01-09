package sysmo.reform.shared.examples

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.runtime.{RFObject, RFRuntime, RuntimeConstructor}

object SkullInventoryBuilder extends ModelBuilder {
  object type_builder extends TypeSystemBuilder {
    /** Data structures */
    val SkullSample = record("SkullSample").label_expr(E.field("code")) +
      f_char("code") +
      f_char("Image type") +
      f_char("Scanned region/object") +
      f_char("sex") +
      f_int("age") +
      f_int("stature") +
      f_int("weight") +
      f_real("BMI") +
      f_char("BMI category") +
      f_char("Filter 1") +
      f_char("Filter 2") +
      f_char("Link Soft") +
      f_char("Link Bone") +
      f_char("Link Body") +
      f_char("Link obj/stl") +
      f_char("Link vgl")


    val SkullInventory = record("SkullInventory") +
      f_array("samples", SkullSample)

  }

  class initializer1[F[+_]](runtime_builder: RuntimeConstructor[F])
    extends Initializer(runtime_builder) {
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
