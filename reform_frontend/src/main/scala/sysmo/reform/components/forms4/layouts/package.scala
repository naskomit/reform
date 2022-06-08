package sysmo.reform.components.forms4

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms4.options.FormRenderingOptions

package object layouts {
  sealed trait ElementWidth
  case object ExtraShort extends ElementWidth
  case object Short extends ElementWidth
  case object Medium extends ElementWidth
  case object Long extends ElementWidth
  case object ExtraLong extends ElementWidth
  case object FullWidth extends ElementWidth
  case class GroupChildElement(child: VdomNode, size: ElementWidth)
  case class ArrayChildElement(child: VdomNode)

  trait FormGroupLayout extends ReactComponent {
    def apply(title: String, children: Seq[GroupChildElement], options: FormRenderingOptions): Unmounted
  }

  trait ArrayGroupLayout extends ReactComponent {
    def apply(title: String, children: Seq[ArrayChildElement], options: FormRenderingOptions): Unmounted
  }
}
