package sysmo.reform.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait Logging {
  lazy val logger = LoggerFactory.getLogger(this.getClass)
}
