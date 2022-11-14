package sysmo.reform.shared.service

import io.circe.DecodingFailure
import sysmo.reform.shared.util.CirceTransport

sealed trait RemoteResult[+T]

case class Ok[+T](value: T) extends RemoteResult[T]
case class Err(msg: String, stacktrace: Seq[String]) extends RemoteResult[Nothing]

object RemoteResult {
  def ok[T](value: T): RemoteResult[T] = Ok(value)
  def err[T](e: Throwable, log: Boolean = false): RemoteResult[T] = {
    val msg = if (e.getMessage == null) e.getClass.getName else e.getMessage
    val stacktrace = e.getStackTrace.map(ste => ste.toString)
    Err(msg, stacktrace)
  }

  def handle_error[T](e: Throwable, log: Boolean = false): RemoteResult[T] = {
    val msg = if (e.getMessage == null) e.getClass.getName else e.getMessage
    val stacktrace = e.getStackTrace.map(ste => ste.toString)
    if (log) {
      System.err.println(msg)
      e.printStackTrace(System.err)
    }
    Err(msg, stacktrace)
  }

  object Transport extends CirceTransport {
    import io.circe.generic.semiauto._
    import io.circe.syntax._

    implicit def encoder_Result[T: Encoder]: Encoder[RemoteResult[T]] = {
      case x @ Err(_, _) => Map("Err" -> x.asJson).asJson
      case x @ Ok(_) => Map("Ok" ->  x.asJson).asJson
    }

    implicit def decoder_Result[T: Decoder]: Decoder[RemoteResult[T]] = { (c: HCursor) =>
      c.keys.toRight(DecodingFailure("No keys present", c.history))
        .flatMap(_.headOption.toRight(DecodingFailure("At least one key must be present", c.history)))
        .flatMap {
          case "Ok" => c.downField("Ok").as[Ok[T]]
          case "Err" => c.downField("Err").as[Err]
          case x => Left(DecodingFailure(s"Unknown Result subtype ${x}", c.history))
        }
    }

    implicit def enc_Ok[T : Encoder]: Encoder[Ok[T]] =
      deriveEncoder[Ok[T]]
    implicit def dec_Ok[T : Decoder]: Decoder[Ok[T]] =
      deriveDecoder[Ok[T]]

    implicit val codec_Err: Codec[Err] = deriveCodec[Err]
  }

}