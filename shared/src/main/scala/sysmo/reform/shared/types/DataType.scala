package sysmo.reform.shared.types

import cats.Show
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.util.CirceTransport
import sysmo.reform.shared.util.containers.FLocal

sealed trait DataType {
  def id: ObjectId
  def show: String
  override def equals(obj: Any): Boolean = obj match {
    case other: DataType => other.id == id
    case _ => false
  }

  override def hashCode(): Int = id.hashCode()
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
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Int extends PrimitiveDataType {
    val symbol = "Int"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Long extends PrimitiveDataType {
    val symbol = "Long"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Char extends PrimitiveDataType {
    val symbol = "Char"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Bool extends PrimitiveDataType {
    val symbol = "Bool"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Date extends PrimitiveDataType {
    val symbol = "Date"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
  }
  case object Id extends PrimitiveDataType {
    val symbol = "Id"
    val id: ObjectId = DataTypeAux.IdSupplier.new_id
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

trait RecordType extends CompoundDataType with HasLabelExpr {
  def fields: Seq[RecordFieldType]
  def field(name: String): Option[RecordFieldType]
  def field_index(name: String): Option[Int]
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


object DataType {
  object Encoders extends CirceTransport {
    import io.circe.syntax._
    implicit val enc_RecordFieldType: Encoder[RecordFieldType] = Encoder.instance(v =>
      Map(
        "name" -> v.name.asJson,
        "optional" -> v.optional.asJson
      ).asJson
    )
  }
}