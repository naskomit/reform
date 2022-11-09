package sysmo.reform.shared

import io.circe.Json
import sysmo.reform.shared.util.Injector

package object logging {
  trait Printer {
    def out(msg: String): Unit
    def warn(msg: String): Unit
    def error(msg: String): Unit
  }

  trait JsonPrinter extends Printer {
    def out(msg: Json): Unit
    def warn(msg: Json): Unit
    def error(msg: Json): Unit
  }

  class Logger(val fmt_str: String, val printer: Printer) {
    def debug(msg: String): Unit = printer.out(format(msg, "DEBUG"))
    def info(msg: String): Unit = printer.out(format(msg, "INFO"))
    def warn(msg: String): Unit = printer.warn(format(msg, "WARN"))
    def error(msg: String): Unit = printer.error(format(msg, "ERROR"))
    def error(err: Throwable): Unit = {
      error(err.getMessage)
      err.getStackTrace.foreach(x => printer.error(x.toString))
    }

    def debug(msg: Json): Unit = printer match {
      case p: JsonPrinter => {
        p.out(format_label("DEBUG"))
        p.out(msg)
      }
      case _ => debug(msg.toString())
    }

    def info(msg: Json): Unit = printer match {
      case p: JsonPrinter => {
        p.out(format_label("INFO"))
        p.out(msg)
      }
      case _ => info(msg.toString())
    }

    def warn(msg: Json): Unit = printer match {
      case p: JsonPrinter => {
        p.warn(format_label("WARN"))
        p.warn(msg)
      }
      case _ => warn(msg.toString())
    }

    def error(msg: Json): Unit = printer match {
      case p: JsonPrinter => {
        p.error(format_label("ERROR"))
        p.error(msg)
      }
      case _ => error(msg.toString())
    }

    def format_label(level: String) = s"[$level/$fmt_str]"
    def format(msg: String, level: String): String = s"[$level/$fmt_str] $msg"
  }

  object Logger {
    def get_logger(klass: Class[_]): Logger = {
      val name = klass.getName.split("\\.").takeRight(1).head
      get_logger(name)
    }

    def get_logger(name: String): Logger = {
      Injector.inject_option[JsonPrinter].map(printer => new Logger(name, printer))
        .orElse(Injector.inject_option[Printer].map(printer => new Logger(name, printer)))
      match {
        case Left(error) => throw error
        case Right(value) => value
      }
    }
  }


  trait Logging {
    lazy val logger: Logger = Logger.get_logger(this.getClass)
  }
}
