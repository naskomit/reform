package sysmo.reform.shared.types

import io.circe.Decoder.Result
import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._

  // TODO
  implicit val enc_RecordFieldType: Encoder[RecordFieldType] = new Encoder[RecordFieldType] {
    override def apply(a: RecordFieldType): Json = ???
  }
  implicit val dec_RecordFieldType: Decoder[RecordFieldType] = new Decoder[RecordFieldType] {
    override def apply(c: HCursor): Result[RecordFieldType] = ???
  }
  //RecordFieldType
}