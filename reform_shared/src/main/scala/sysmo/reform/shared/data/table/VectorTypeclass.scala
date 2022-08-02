package sysmo.reform.shared.data.table

sealed trait VectorType
object VectorType {
  case object Int extends VectorType
  case object Real extends VectorType
  case object Bool extends VectorType
  case object Char extends VectorType

  def with_name(name: String): VectorType= {
    name match {
      case "Int" => Int
      case "Real" => Real
      case "Bool" => Bool
      case "Char" => Char
    }
  }
}

trait VectorTypeclass[V] {
  type ValueType = V
  type MutableStorage <: VectorStorage[ValueType]
  type ImmutableStorage <: VectorStorage[ValueType]
  val tpe: VectorType
  def create_mutable_storage(manager: TableManager, name: String): MutableStorage
}
