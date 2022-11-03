package sysmo.reform.shared.util

trait CirceTransport {
  import io.circe
  import io.circe.generic.extras.Configuration

  //  val Configuration = extras.Configuration
  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("$type")

  type Codec[A] = circe.Codec[A]
  type Encoder[A] = circe.Encoder[A]
  type Decoder[A] = circe.Decoder[A]
  protected val Encoder = circe.Encoder
  protected val Decoder = circe.Decoder
  type HCursor = circe.HCursor
  type Json = circe.Json
  protected val Json = circe.Json
}
