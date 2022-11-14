package sysmo.reform.shared.query

import cats.implicits._
import io.circe.Decoder.Result
import io.circe.{Codec, DecodingFailure}
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.types.PrimitiveDataType
import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._
  import sysmo.reform.shared.data.Transport._
  import sysmo.reform.shared.expr.Transport._

  implicit val codec_Query: Codec[Query] = deriveCodec[Query]
  implicit val codec_BasicQuery: Codec[BasicQuery] = deriveCodec[BasicQuery]

  implicit val codec_QuerySource: Codec[QuerySource] = deriveCodec[QuerySource]
  implicit val codec_Projection: Codec[Projection] = deriveCodec[Projection]
  implicit val codec_Fields: Codec[Fields] = deriveCodec[Fields]
  implicit val codec_QueryFilter: Codec[QueryFilter] = deriveCodec[QueryFilter]
  implicit val codec_QuerySort: Codec[QuerySort] = deriveCodec[QuerySort]
  implicit val codec_ColumnSort: Codec[FieldSort] = deriveCodec[FieldSort]
  implicit val codec_QueryRange: Codec[QueryRange] = deriveCodec[QueryRange]
}
