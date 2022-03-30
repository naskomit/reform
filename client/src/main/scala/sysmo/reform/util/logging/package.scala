package sysmo.reform.util

import org.scalajs.dom

package object log {
  trait Logger {
    def debug(msg: String): Unit
    def info(msg: String): Unit
    def warn(msg: String): Unit
    def error(msg: String): Unit
    def error(err: Throwable): Unit
  }


  object Logger {
    class Default(fmt_str: String) extends Logger {
      def format(msg: String, level: String) = f"[$level/$fmt_str] $msg"
      def debug(msg: String): Unit = dom.console.log(format(msg, "DEBUG"))
      def info(msg: String): Unit= dom.console.log(format(msg, "INFO"))
      def warn(msg: String): Unit= dom.console.warn(format(msg, "WARN"))
      def error(msg: String): Unit= dom.console.error(format(msg, "ERROR"))
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
    lazy val logger = Logger.get_logger(this.getClass)
  }
}
