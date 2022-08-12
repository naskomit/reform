package sysmo.reform.shared.types

import sysmo.reform.shared.expr.Expression

sealed trait DataType {
  def symbol: String
}

trait DataTypeBuilder[DT] {
  def builder: this.type = this
  def build: DT
}

sealed trait AtomicDataType extends DataType {

}

object AtomicDataType {
  case object Real extends AtomicDataType {
    val symbol = "Real"
  }
  case object Int extends AtomicDataType {
    val symbol = "Int"
  }
  case object Long extends AtomicDataType {
    val symbol = "Long"
  }
  case object Char extends AtomicDataType {
    val symbol = "Char"
  }
  case object Bool extends AtomicDataType {
    val symbol = "Bool"
  }
  case object Date extends AtomicDataType {
    val symbol = "Date"
  }
  case object Id extends AtomicDataType {
    val symbol = "Id"
  }

  def apply(name: String): AtomicDataType= {
    name match {
      case "Real" => Real
      case "Int" => Int
      case "Long" => Long
      case "Char" => Char
      case "Bool" => Bool
      case "Date" => Date
      case "Id" => Id
    }
  }
}

sealed trait CompoundDataType extends DataType

trait RecordType extends CompoundDataType {
  def symbol: String
  def fields: Seq[RecordField]
  def field(name: String): Option[RecordField]
  def label_expr: Option[Expression]
}

object RecordType extends RecordTypeImpl

trait RecordField {
  def name: String
  def label: Option[String]
  def make_label: String = label.getOrElse(name)
  def ftype: DataType
  def nullable: Boolean
}

object RecordField extends RecordFieldImpl
