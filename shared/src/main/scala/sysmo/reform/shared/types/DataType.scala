package sysmo.reform.shared.types

import sysmo.reform.shared.data.{ObjectId}
import sysmo.reform.shared.expr.Expression

sealed trait DataType {
  def id: ObjectId

  override def equals(obj: Any): Boolean = obj match {
    case other: DataType => other.id == id
    case _ => false
  }

  override def hashCode(): Int = id.hashCode()
}

/** AtomicDataType */
sealed trait AtomicDataType extends DataType

object AtomicDataType extends AtomicDataTypeAux {
  case object Real extends AtomicDataType {
    val symbol = "Real"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Int extends AtomicDataType {
    val symbol = "Int"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Long extends AtomicDataType {
    val symbol = "Long"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Char extends AtomicDataType {
    val symbol = "Char"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Bool extends AtomicDataType {
    val symbol = "Bool"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Date extends AtomicDataType {
    val symbol = "Date"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
  }
  case object Id extends AtomicDataType {
    val symbol = "Id"
    val descr = Some("")
    val id = DataTypeAux.IdSupplier.new_id
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

sealed trait CompoundDataType extends DataType with HasSymbol

trait RecordType extends CompoundDataType with HasLabelExpr {
  def fields: Seq[RecordFieldType]
  def field(name: String): Option[RecordFieldType]
}

object RecordType extends RecordTypeAux

trait RecordFieldType extends HasName with HasLabelExpr {
  def dtype: DataType
  def optional: Boolean
}

object RecordFieldType extends RecordFieldTypeAux

trait UnionType extends CompoundDataType {
  def subtypes: Seq[RecordType]
  def supertype_of(r: RecordType): Boolean
}

object UnionType extends UnionTypeAux

trait ArrayType extends DataType with HasLabelExpr {
  def prototype: CompoundDataType
}

object ArrayType extends ArrayTypeAux

trait ReferenceType extends DataType {
  def prototype: CompoundDataType
}

object ReferenceType extends ReferenceTypeAux

trait MultiReferenceType extends DataType {
  def prototype: CompoundDataType
}

object MultiReferenceType extends MultiReferenceTypeAux