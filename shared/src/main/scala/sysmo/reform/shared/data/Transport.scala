package sysmo.reform.shared.data

import io.circe.Decoder.Result
import io.circe.{DecodingFailure, JsonObject}
import sysmo.reform.shared.util.CirceTransport
import sysmo.reform.shared.data.Value._
import sysmo.reform.shared.types.PrimitiveDataType

import java.util.Date

object Transport extends CirceTransport {
  import io.circe.syntax._
  import Value.implicits._

  implicit val enc_Id: Encoder[ObjectId] =
    Encoder.instance(_.serialize.asJson)

  implicit val enc_Date: Encoder[Date] =
    Encoder.instance(date => date.getTime.asJson)

  def opt_v_asJson[T : Encoder](opt_v: Option[T]): Json =
    opt_v.map(_.asJson).getOrElse(Json.Null)


  implicit val enc_Value: Encoder[Value] =
    Encoder.instance{
      case NoValue => Json.Null
      case RealValue(opt_v) => opt_v_asJson(opt_v)
      case IntValue(opt_v) => opt_v_asJson(opt_v)
      case LongValue(opt_v) => opt_v_asJson(opt_v)
      case BoolValue(opt_v) => opt_v_asJson(opt_v)
      case CharValue(opt_v) => opt_v_asJson(opt_v)
      case DateValue(opt_v) => opt_v_asJson(opt_v)
      case IdValue(opt_v) => opt_v_asJson(opt_v)
    }

  // Value decoder without a schema
  implicit val dec_Value: Decoder[Value] = Decoder.instance {c =>
    if (c.value.isNull) {
      Right(Value.empty)
    } else if (c.value.isNumber) {
      c.value.as[Double].map(Value(_))
    } else if (c.value.isString) {
      c.value.as[String].map(Value(_))
    } else {
      Left(DecodingFailure(s"Cannot decode Value ${c.value}", c.history))
    }
  }

  // Value decoder with a schema
  class ValueDecoder(dtype: PrimitiveDataType) extends Decoder[Value] {
    override def apply(c: HCursor): Result[Value] = {
      if (c.value.isNull) {
        Right(Value.empty)
      } else dtype match {
        case PrimitiveDataType.Real => c.as[Double].map(Value(_))
        case PrimitiveDataType.Int => c.as[Int].map(Value(_))
        case PrimitiveDataType.Long => c.as[Long].map(Value(_))
        case PrimitiveDataType.Char => c.as[String].map(Value(_))
        case PrimitiveDataType.Bool => c.as[Boolean].map(Value(_))
        case PrimitiveDataType.Date => Left(DecodingFailure(s"Decoding Date not implemented", c.history))
        case PrimitiveDataType.Id => Left(DecodingFailure(s"Decoding Id not implemented", c.history))
      }
    }
  }

}
