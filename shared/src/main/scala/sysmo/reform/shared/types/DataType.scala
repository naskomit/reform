package sysmo.reform.shared.types

import cats.Show
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.util.{CirceTransport, SequenceIndex}
import sysmo.reform.shared.util.containers.FLocal

sealed trait DataType extends Product with Serializable {
//  def id: ObjectId
  def show: String
//  override def equals(obj: Any): Boolean = obj match {
//    case other: DataType => other.id == id
//    case _ => false
//  }
//
//  override def hashCode(): Int = id.hashCode()
}

/** PrimitiveDataType */
sealed trait PrimitiveDataType extends DataType {
  def default: Value = Value.empty
  val symbol: String
  def show: String = symbol
}

object PrimitiveDataType extends PrimitiveDataTypeAux {
  case object Real extends PrimitiveDataType {
    val symbol = "Real"
  }
  case object Int extends PrimitiveDataType {
    val symbol = "Int"
  }
  case object Long extends PrimitiveDataType {
    val symbol = "Long"
  }
  case object Char extends PrimitiveDataType {
    val symbol = "Char"
  }
  case object Bool extends PrimitiveDataType {
    val symbol = "Bool"
  }
  case object Date extends PrimitiveDataType {
    val symbol = "Date"
  }
  case object Id extends PrimitiveDataType {
    val symbol = "Id"
  }

  def apply(name: String): FLocal[PrimitiveDataType] = {
    name match {
      case "Real" => FLocal(Real)
      case "Int" => FLocal(Int)
      case "Long" => FLocal(Long)
      case "Char" => FLocal(Char)
      case "Bool" => FLocal(Bool)
      case "Date" => FLocal(Date)
      case "Id" => FLocal(Id)
      case x => FLocal.error(new IllegalArgumentException(s"Not a valid primitive type ${x}"))
    }
  }
}

sealed trait CompoundDataType extends DataType with HasSymbol

case class RecordType(symbol: String, descr: Option[String], fields: Seq[RecordFieldType], label_expr: Option[Expression])
  extends CompoundDataType with HasLabelExpr {
  private lazy val _field_index: SequenceIndex[String, RecordFieldType] =
    new SequenceIndex(fields, _.name)
  def field_index(name: String): Option[Int] = _field_index.get_index(name)
  def field(name: String): Option[RecordFieldType] = _field_index.get(name)
  override def show: String = s"Record[${symbol}]"
}

object RecordType extends RecordTypeAux

case class RecordFieldType(val name: String, val descr: Option[String],
                           val dtype: DataType, val optional: Boolean,
                           val label_expr: Option[Expression])
  extends HasName with HasLabelExpr {
}

object RecordFieldType extends RecordFieldTypeAux

case class UnionType(symbol: String, descr: Option[String], subtypes: Seq[RecordType])
  extends CompoundDataType {
  def supertype_of(r: RecordType): Boolean = subtypes.contains(r)
  override def show: String = s"Union[${symbol}]"
}

object UnionType extends UnionTypeAux

case class ArrayType(prototype: CompoundDataType, label_expr: Option[Expression]) extends DataType with HasLabelExpr {
  override def show: String = s"Array[${prototype.symbol}]"
}

object ArrayType extends ArrayTypeAux

case class ReferenceType(prototype: CompoundDataType) extends DataType {
  override def show: String = s"Ref[${prototype.symbol}]"
}

object ReferenceType extends ReferenceTypeAux

case class MultiReferenceType(prototype: CompoundDataType) extends DataType {
  override def show: String = s"MultiRef[${prototype.symbol}]"
}

object MultiReferenceType extends MultiReferenceTypeAux
