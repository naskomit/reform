package sysmo.reform.util

import io.circe.Json
import org.scalajs.dom
import sysmo.reform.util.json.circe_2_js

object log {
  trait Logger {
    def debug(msg: String): Unit
    def debug(msg: Json): Unit
    def info(msg: String): Unit
    def info(msg: Json): Unit
    def warn(msg: String): Unit
    def warn(msg: Json): Unit
    def error(msg: String): Unit
    def error(msg: Json): Unit
    def error(err: Throwable): Unit
  }


  object Logger {
    class Default(fmt_str: String) extends Logger {
      def format(msg: String, level: String): String = s"[$level/$fmt_str] $msg"
      def format(msg: Json, level: String): String = s"[$level/$fmt_str]"
      def debug(msg: String): Unit = dom.console.log(format(msg, "DEBUG"))
      def debug(msg: Json): Unit = dom.console.log(format(msg, "DEBUG"), circe_2_js(msg))
      def info(msg: String): Unit= dom.console.log(format(msg, "INFO"))
      def info(msg: Json): Unit = dom.console.log(format(msg, "INFO"), circe_2_js(msg))
      def warn(msg: String): Unit= dom.console.warn(format(msg, "WARN"))
      def warn(msg: Json): Unit = dom.console.warn(format(msg, "WARN"), circe_2_js(msg))
      def error(msg: String): Unit= dom.console.error(format(msg, "ERROR"))
      def error(msg: Json): Unit = dom.console.error(format(msg, "ERROR"), circe_2_js(msg))
      def error(err: Throwable): Unit = {
        val msg = err.getMessage
        System.err.println(format(msg, "ERROR"))
        err.getStackTrace.foreach(System.err.println)
      }
    }

    def get_logger(klass: Class[_]): Logger = {
      val name = klass.getName.split("\\.").takeRight(1).head
      get_logger(name)
    }
    def get_logger(name: String): Logger = new Default(name)
  }

  trait Logging {
    lazy val logger: Logger = Logger.get_logger(this.getClass)
  }
}
