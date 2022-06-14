package sysmo.reform.components.forms4

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions
import sysmo.reform.shared.{form => F}

package object layouts {
  sealed trait SizeHint
  case object ExtraShort extends SizeHint
  case object Short extends SizeHint
  case object Medium extends SizeHint
  case object Long extends SizeHint
  case object ExtraLong extends SizeHint
  case object FullWidth extends SizeHint
  case class GroupChildNode(node: FormRenderingOptions => VdomElement, title: Option[String], size_hint: SizeHint, options: FormRenderingOptions)
  case class ArrayChildElement(child: VdomNode)

  trait FormGroupLayout extends ReactComponent {
    def apply(children: Seq[GroupChildNode], options: FormRenderingOptions): Unmounted
  }

  trait ArrayGroupLayout extends ReactComponent {
    def apply(title: String, children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted
  }
}
