package sysmo.reform.shared.service

case class ServiceError(msg: String, stacktrace: Seq[String]) extends Throwable {

}
