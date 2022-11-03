package sysmo.reform.shared.types

import io.circe.Decoder.{Result => DecResult}
import io.circe.DecodingFailure
import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._

  // Codec for DataType
  implicit val enc_DataType: Encoder[DataType] =
    Encoder.instance {dtype =>
      val dtype_ser = dtype match {
        case dt: PrimitiveDataType =>
          Map("kind" -> "primitive", "type" -> dt.symbol).asJson
        case dt: CompoundDataType => dt match {
          case recordType: RecordType =>
            Map("kind" -> "record", "type" -> recordType.symbol).asJson
          case unionType: UnionType =>
            Map("kind" -> "union", "type" -> unionType.symbol).asJson
        }
        case dt: ArrayType =>
          Map("kind" -> "array", "type" -> dt.prototype.symbol).asJson
        case dt: ReferenceType => ???
        case dt: MultiReferenceType => ???
      }
      dtype_ser
    }

//  implicit val dec_DataType: Decoder[DataType] =
//    Decoder.instance { dtype =>
//      for {
//        kind <- dtype.downField("kind").as[String]
//        tpe <- dtype.downField("type").as[String]
//        data_type <- {
//          val result = kind match {
//            case "primitive" =>
//              PrimitiveDataType(tpe) match {
//                case Left(error) => Left(DecodingFailure(
//                  s"Unknown primitive type ${tpe}", dtype.history
//                ))
//
//                case Right(dt) => Right(dt)
//              }
//
//            //            case "record" =>
//            //            case "union" =>
//            //            case "array" =>
//            case _ => Left(DecodingFailure(
//              s"Unknown kind ${kind}", dtype.history
//            ))
//
//          }
//          result
//        }
//      } yield data_type
//    }

  implicit val enc_RecordFieldType: Encoder[RecordFieldType] =
    Encoder.instance {ftype =>
      Map(
        "name" -> ftype.name.asJson,
        "dtype" -> ftype.dtype.asJson,
        "optional" -> ftype.optional.asJson
      ).asJson
    }

  // TODO
  implicit val dec_RecordFieldType: Decoder[RecordFieldType] = new Decoder[RecordFieldType] {
    override def apply(c: HCursor): DecResult[RecordFieldType] = ???
  }

  implicit val enc_RecordType: Encoder[RecordType] = Encoder.instance {rec_type =>
    val fields = Json.fromValues(rec_type.fields.map(_.asJson))
    Map(
      "symbol" -> rec_type.symbol.asJson,
      "fields" -> fields
    ).asJson
  }
}