package sysmo.reform.shared.runtime

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.types.RecordType

object Instantiation {
  trait InstanceBuilder[F[+_]] {
    def build: RuntimeObject[F]
  }

  case class AtomicBuilder[F[+_]](value: Value) extends InstanceBuilder[F] {
    override def build: RuntimeObject[F] = ???
  }

  case class ArrayBuilder[F[+_]](children: Seq[InstanceBuilder[F]])
    extends InstanceBuilder[F] {
    override def build: RuntimeObject[F] = ???
  }

  case class GroupBuilder[F[+_]](dtype: RecordType, children: Seq[(String, InstanceBuilder[F])])
    extends InstanceBuilder[F] {
    override def build: RuntimeObject[F] = ???
  }

  implicit class RecordBuilder(dtype: RecordType.Builder) {
    def apply[F[+_]](children: (String, InstanceBuilder[F])*): GroupBuilder[F] = {
      GroupBuilder(dtype, children)
    }
  }

  implicit def seq2builder[F[+_]](x: Seq[InstanceBuilder[F]]): ArrayBuilder[F] = ArrayBuilder(x)

  implicit def value2builder[F[+_]](x: Value): AtomicBuilder[F] = AtomicBuilder(x)
  import Value.implicits._
  implicit def str2ab[F[+_]](x: String): AtomicBuilder[F] = AtomicBuilder(x)
  implicit def int2ab[F[+_]](x: Int): AtomicBuilder[F] = AtomicBuilder(x)

  implicit class RuntimeInst[F[+_]](rt: ObjectRuntime[F]) {
    def instantiate(x: InstanceBuilder[F]): RuntimeObject[F] = x.build
  }
}
