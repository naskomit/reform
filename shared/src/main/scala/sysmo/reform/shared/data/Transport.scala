package sysmo.reform.shared.data

import io.circe.JsonObject
import sysmo.reform.shared.util.CirceTransport
import sysmo.reform.shared.data.Value._

object Transport extends CirceTransport {
  import io.circe.syntax._

  implicit val enc_Id: Encoder[ObjectId] =
    Encoder.instance{
      case UUObjectId(v) => Map(
        "$type" -> "UUID".asJson,
        "value" -> v.toString.asJson
      ).asJson
      case _ => null
    }

  implicit val enc_Value: Encoder[Value] =
    Encoder.instance{
      case RealValue(Some(v)) => v.asJson
      case IntValue(Some(v)) => v.asJson
      case LongValue(Some(v)) => v.asJson
      case BoolValue(Some(v)) => v.asJson
      case CharValue(Some(v)) => v.asJson
      case DateValue(Some(v)) =>
        Map(
          "$type" -> "DateValue".asJson,
          "value" -> v.toString.asJson
        ).asJson
      case IdValue(Some(v)) => v.asJson
    }

  // TODO
  implicit val dec_Value: Decoder[Value] =
    (x : HCursor) => x.as[Json].map(x => NoValue)

}
