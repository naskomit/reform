package sysmo.reform.shared.query

import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
//  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._

  /** ## Query Expression */
  implicit val codec_ColumnRef: Codec[ColumnRef] = deriveCodec[ColumnRef]

  implicit val enc_Value: Encoder[Val] = Encoder.instance {
    case Val(y: Int) => y.asJson
    case Val(y: Double) => y.asJson
    case Val(y: Boolean) => y.asJson
    case Val(y: String) => y.asJson
    case Val(y) => throw new IllegalStateException(f"Cannot handle value $y")
  }

  implicit val dec_Value: Decoder[Val] = Decoder.instance (x => {
    x.as[Int].orElse(x.as[Double]).orElse(x.as[Boolean]).orElse(x.as[String]).map(Val)
  })

  implicit val codec_LogicalAnd: Codec[LogicalAnd] = deriveCodec
  implicit val codec_LogicalOr: Codec[LogicalOr] = deriveCodec
  implicit val codec_LogicalNot: Codec[LogicalNot] = deriveCodec

  implicit val enc_NumericalPredicateOp: Encoder[NumericalPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_NumericalPredicateOp: Decoder[NumericalPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(NumericalPredicateOp.withName)

  implicit val codec_NumericalPredicate: Codec[NumericalPredicate] = deriveCodec[NumericalPredicate]

  implicit val enc_StringPredicateOp: Encoder[StringPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_StringPredicateOp: Decoder[StringPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(StringPredicateOp.withName)

  implicit val codec_StringPredicate: Codec[StringPredicate] = deriveCodec[StringPredicate]

  implicit val enc_ContainmentPredicateOp: Encoder[ContainmentPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_ContainmentPredicateOp: Decoder[ContainmentPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(ContainmentPredicateOp.withName)

  implicit val codec_ContainmentPredicate: Codec[ContainmentPredicate] = deriveCodec[ContainmentPredicate]

  implicit val codec_PredicateExpression: Codec[PredicateExpression] = deriveCodec[PredicateExpression]

  implicit val codec_Expression: Codec[Expression] = deriveCodec[Expression]

  /** ## Query  Filter */
  implicit val codec_QueryFilter: Codec[QueryFilter] = deriveCodec

  /** ## Query Sort */
  implicit val codec_ColumnSort: Codec[ColumnSort] = deriveCodec
  implicit val codec_QuerySort: Codec[QuerySort] = deriveCodec

  /** ## Query Source */
  implicit val codec_SingleTable: Codec[SingleTable] = deriveCodec
  implicit val codec_QuerySource: Codec[QuerySource] = deriveCodec

  /** ## Query Range */
  implicit val codec_QueryRange: Codec[QueryRange] = deriveCodec
  /** ## Query */
  implicit val codec_BasicQuery: Codec[BasicQuery] = deriveCodec
  implicit val codec_Query: Codec[Query] = deriveCodec

}