package sysmo.reform.shared.field

import io.circe.{Decoder, JsonObject}

import scala.reflect.ClassTag

sealed trait FieldType {

}

object FieldType {
  case object Int extends FieldType
  case object Real extends FieldType
  case object Bool extends FieldType
  case object Char extends FieldType
  case object Record extends FieldType

  def apply(name: String): FieldType= {
    name match {
      case "Int" => Int
      case "Real" => Real
      case "Bool" => Bool
      case "Char" => Char
      case "Record" => Record
    }
  }
}

//trait VectorTypeclass[V] {
//  type ValueType = V
//  type MutableStorage <: VectorStorage[ValueType]
//  type ImmutableStorage <: VectorStorage[ValueType]
//  val tpe: VectorType
//  def create_mutable_storage(manager: TableManager, name: String): MutableStorage
//}
