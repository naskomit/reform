package sysmo.reform.shared.examples

import sysmo.reform.shared.runtime.{LocalRuntime, RFObject, RFRuntime, RuntimeConstructor}
import sysmo.reform.shared.types.{TypeSystem}
import sysmo.reform.shared.types

trait ModelBuilder {
  type TypeSystemBuilder = types.TypeSystemBuilder
  val type_builder: TypeSystemBuilder
  def type_system: TypeSystem = type_builder.build

  abstract class Initializer[_F[+X]](runtime_builder: RuntimeConstructor[_F]) {
    import sysmo.reform.shared.runtime.Instantiation
    val inst: Instantiation[_F] = new Instantiation(runtime_builder(type_system))
    val root: _F[RFObject[_F]]
  }
}
