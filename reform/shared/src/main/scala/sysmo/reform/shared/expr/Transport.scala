package sysmo.reform.shared.expr

import io.circe.Codec
import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._
  import sysmo.reform.shared.data.Transport._

  implicit val codec_ColumnRef: Codec[ColumnRef] = deriveCodec[ColumnRef]
  implicit val enc_Constant: Encoder[Constant] = deriveCodec[Constant]

  implicit val codec_LogicalAnd: Codec[LogicalAnd] = deriveCodec
  implicit val codec_LogicalOr: Codec[LogicalOr] = deriveCodec
  implicit val codec_LogicalNot: Codec[LogicalNot] = deriveCodec

  implicit val enc_CommonPredicateOp: Encoder[CommonPredicateOp] =
    Encoder.instance{
      case Equal => "==".asJson
      case NotEqual => "!=".asJson
    }

  implicit val dec_CommonPredicateOp: Decoder[CommonPredicateOp] =
    (x : HCursor) => x.as[String].map {
      case "==" => Equal
      case "!=" => NotEqual
    }

  implicit val enc_NumericalPredicateOp: Encoder[NumericalPredicateOp] =
    Encoder.instance{
      case NP_> => ">".asJson
      case NP_>= => ">=".asJson
      case NP_< => "<".asJson
      case NP_<= => "<=".asJson
    }

  implicit val dec_NumericalPredicateOp: Decoder[NumericalPredicateOp] =
    (x : HCursor) => x.as[String].map {
      case ">" => NP_>
      case ">=" => NP_>=
      case "<" => NP_<
      case "<=" => NP_<=
    }

  implicit val codec_NumericalPredicate: Codec[NumericalPredicate] = deriveCodec[NumericalPredicate]

  implicit val enc_StringPredicateOp: Encoder[StringPredicateOp] =
    Encoder.instance {
      case StartingWith => "StartingWith".asJson
      case NonStartingWith => "NonStartingWith".asJson
      case EndingWith => "EndingWith".asJson
      case NotEndingWith => "NotEndingWith".asJson
      case Containing => "Containing".asJson
      case NotContaining => "NotContaining".asJson
    }

  implicit val dec_StringPredicateOp: Decoder[StringPredicateOp] =
    (x : HCursor) => x.as[String].map {
      case "StartingWith" => StartingWith
      case "NonStartingWith" => NonStartingWith
      case "EndingWith" => EndingWith
      case "NotEndingWith" => NotEndingWith
      case "Containing" => Containing
      case "NotContaining" => NotContaining

    }

  implicit val codec_StringPredicate: Codec[StringPredicate] = deriveCodec[StringPredicate]

  implicit val enc_ContainmentPredicateOp: Encoder[ContainmentPredicateOp] =
    Encoder.instance {
      case Within => "Within".asJson
      case Without => "Without".asJson
    }

  implicit val dec_ContainmentPredicateOp: Decoder[ContainmentPredicateOp] =
    (x : HCursor) => x.as[String].map {
      case "Within" => Within
      case "Without" => Without
    }

//  implicit val codec_ContainmentPredicate: Codec[ContainmentPredicate] = deriveCodec[ContainmentPredicate]

  implicit val codec_PredicateExpression: Codec[PredicateExpression] = deriveCodec[PredicateExpression]

  implicit val codec_Expression: Codec[Expression] = deriveCodec[Expression]
}
