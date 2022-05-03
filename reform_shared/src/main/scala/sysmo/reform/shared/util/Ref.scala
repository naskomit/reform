package sysmo.reform.shared.util

trait Ref[+A] {
  def _target: A
  lazy val target: A = _target
  def _uid: String
  lazy val uid: String = _uid
}
