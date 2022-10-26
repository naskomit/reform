package sysmo.reform.shared.examples

import sysmo.reform.shared.runtime.{LocalRuntime, RFObject, RFRuntime, RuntimeConstructor}
import sysmo.reform.shared.types.{TypeSystem, TypeSystemBuilder}

trait ModelBuilder {
  type TypeBuilder = TypeSystemBuilder

  abstract class Initializer[_F[+X]](runtime_builder: RuntimeConstructor[_F], type_system: TypeSystem) {
    import sysmo.reform.shared.runtime.Instantiation
    val inst: Instantiation[_F] = new Instantiation(runtime_builder(type_system))
    val root: _F[RFObject[_F]]
  }
}
