package sysmo.reform.shared.util


case class Named[V](
  name: String,
  label: Option[String],
  value: V
) {
  def make_label: String = label.getOrElse(name)
}

object Named {
  import io.circe.Codec
  import io.circe.generic.semiauto._
  implicit def codec_NamedItem[A : Codec]: Codec[Named[A]] = deriveCodec[Named[A]]
}