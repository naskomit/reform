package sysmo.reform.shared.examples

import sysmo.reform.shared.runtime.{LocalRuntime, RFObject, RFRuntime}
import sysmo.reform.shared.types.TypeSystemBuilder

trait ModelBuilder {
  type TypeBuilder = TypeSystemBuilder

  abstract class Initializer[_F[+X]](runtime: RFRuntime[_F]) {
    import sysmo.reform.shared.runtime.Instantiation
    val inst = new Instantiation(runtime)
    val root: _F[RFObject[_F]]
  }
}
