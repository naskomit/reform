package sysmo.reform.util

import org.scalajs.dom

package object log {
  trait Logger {
    def debug(msg: String): Unit
    def info(msg: String): Unit
    def warn(msg: String): Unit
    def error(msg: String): Unit
  }


  object Logger {
    class Default(fmt_str: String) extends Logger {
      def format(msg: String) = f"[$fmt_str] $msg"
      def debug(msg: String) = dom.console.log(format(msg))
      def info(msg: String)= dom.console.log(format(msg))
      def warn(msg: String)= dom.console.warn(format(msg))
      def error(msg: String)= dom.console.error(format(msg))
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
