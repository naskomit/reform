package sysmo.reform.components.forms.options

import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

trait Rule[-A, +B] {
  def default: B = ???
  def apply(x: A): Option[B]
  def apply_or_default(x: A): B = apply(x).getOrElse(default)
}

object FormRenderingRules {
  val show_title = new Rule[FR.RuntimeObject, Boolean] {
    override def default: Boolean = true
    def apply(x: FR.RuntimeObject): Option[Boolean] = x.parent match {
      case Some(_: FR.Array) => Some(false)
      case _ => None
    }
  }
}
