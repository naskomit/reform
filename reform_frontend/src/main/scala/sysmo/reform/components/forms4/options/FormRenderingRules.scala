package sysmo.reform.components.forms4.options

import sysmo.reform.shared.{form => F}

trait Rule[-A, +B] {
  def default: B = ???
  def apply(x: A): Option[B]
  def apply_or_default(x: A): B = apply(x).getOrElse(default)
}

object FormRenderingRules {
  val show_title = new Rule[F.FormElement, Boolean] {
    override def default: Boolean = true
    def apply(x: F.FormElement): Option[Boolean] = x.parent match {
      case Some(F.GroupArray(_)) => Some(false)
      case _ => None
    }
  }
}
