package sysmo.reform.shared.examples

import sysmo.reform.shared.runtime.{LocalRuntime, RuntimeObject}
import sysmo.reform.shared.types.TypeSystemBuilder

trait ModelBuilder {
  type TypeBuilder = TypeSystemBuilder

  trait Initializer {
    val runtime: LocalRuntime = LocalRuntime()
    type F[+X] = runtime.F[X]
    import sysmo.reform.shared.runtime.Instantiation
    val inst = new Instantiation(runtime)
    def apply(): F[RuntimeObject[F]]
  }
}
