package sysmo.reform.shared.data

import io.circe.Decoder.Result
import io.circe.JsonObject
import sysmo.reform.shared.util.CirceTransport
import sysmo.reform.shared.data.Value._

import java.util.Date

object Transport extends CirceTransport {
  import io.circe.syntax._

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

  implicit object ValueDecoder extends Decoder[Value] {
    override def apply(c: HCursor): Result[Value] = ???
  }

}
