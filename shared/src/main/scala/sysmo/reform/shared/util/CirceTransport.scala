package sysmo.reform.shared.util

trait CirceTransport {
  import io.circe
  import io.circe.generic.extras.Configuration

  //  val Configuration = extras.Configuration
  implicit protected val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("$type")

  protected type Codec[A] = circe.Codec[A]
  protected type Encoder[A] = circe.Encoder[A]
  protected type Decoder[A] = circe.Decoder[A]
  protected val Encoder = circe.Encoder
  protected val Decoder = circe.Decoder
  protected type HCursor = circe.HCursor
  protected type Json = circe.Json
  protected val Json = circe.Json
}
