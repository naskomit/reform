package sysmo.reform.shared.util

trait NamedBase {
  val name: String
  val label: Option[String]
  def make_label: String = label.getOrElse(name)
}


case class Named(name: String, label: Option[String]) extends NamedBase

case class NamedValue[V](name: String, label: Option[String], value: V) extends NamedBase{
}

sealed trait TNamed[A]
object TNamed {
  implicit val TNamed_String: TNamed[String] = new TNamed[String] {}
  implicit val TNamed_Named: TNamed[Named] = new TNamed[Named] {}
}

trait ILabeled[+V] {
  val value: V
  val label: Option[String]
  def make_label: String = label.getOrElse(value.toString)
}
case class LabeledValue[+V](value: V, label: Option[String] = None) extends ILabeled[V]

object NamedValue {
  import io.circe.Codec
  import io.circe.generic.semiauto._
  implicit def codec_NamedItem[A : Codec]: Codec[NamedValue[A]] = deriveCodec[NamedValue[A]]
}