package sysmo.reform.shared.types

import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._
  import sysmo.reform.shared.expr.Transport._

  implicit val codec_Real: Codec[PrimitiveDataType] = deriveCodec[PrimitiveDataType]
  implicit val codec_CompoundType: Codec[CompoundDataType] = deriveCodec[CompoundDataType]
  implicit val codec_RecType: Codec[RecordType] = deriveCodec[RecordType]
  implicit val codec_RecField: Codec[RecordFieldType] = deriveCodec[RecordFieldType]
  implicit val codec_UnionType: Codec[UnionType] = deriveCodec[UnionType]
  implicit val codec_ArrayType: Codec[ArrayType] = deriveCodec[ArrayType]
  implicit val codec_ReferenceType: Codec[ReferenceType] = deriveCodec[ReferenceType]
  implicit val codec_MultiReferenceType: Codec[MultiReferenceType] = deriveCodec[MultiReferenceType]
  implicit val codec_DataType: Codec[DataType] = deriveCodec[DataType]
}