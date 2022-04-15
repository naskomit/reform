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

class FuncLogger(logger : Logger) {
  type F = Either[Throwable, Unit]
  def debug(msg: String): F = {
    logger.debug(msg)
    Right()
  }

  def info(msg: String): F = {
    logger.info(msg)
    Right()
  }
  def warn(msg: String): F = {
    logger.warn(msg)
    Right()
  }
  def error(msg: String): F = {
    logger.error(msg)
    Right()
  }
}

trait Logging {
  lazy val logger = LoggerFactory.getLogger(this.getClass)
}

trait FuncLogging {
  lazy val logger = new FuncLogger(LoggerFactory.getLogger(this.getClass))
}


trait AsyncLogging {
  lazy val logger = new AsyncLogger(LoggerFactory.getLogger(this.getClass))
}
