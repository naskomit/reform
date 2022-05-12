package sysmo.reform.shared.query

import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
//  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._
  import sysmo.reform.shared.expr.Transport._

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