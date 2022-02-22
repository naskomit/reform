package sysmo.reform.util

import monix.eval.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AsyncLogger(logger : Logger) {
  def debug(msg: String): Task[Unit] = Task {
    logger.debug(msg)
  }
  def info(msg: String): Task[Unit] = Task {
    logger.info(msg)
  }
  def warn(msg: String): Task[Unit] = Task {
    logger.warn(msg)
  }
  def error(msg: String): Task[Unit] = Task {
    logger.error(msg)
  }
}

trait Logging {
  lazy val logger = LoggerFactory.getLogger(this.getClass)
}

trait AsyncLogging {
  lazy val logger = new AsyncLogger(LoggerFactory.getLogger(this.getClass))
}
