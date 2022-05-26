package sysmo.reform.components.forms4

import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import sysmo.reform.components.forms4.options.FormRenderingOptions

package object layouts {
  sealed trait ElementWidth
  case object ExtraShort extends ElementWidth
  case object Short extends ElementWidth
  case object Medium extends ElementWidth
  case object Long extends ElementWidth
  case object ExtraLong extends ElementWidth
  case object FullWidth extends ElementWidth
  case class ChildElement(child: VdomNode, size: ElementWidth)

  trait FormGroupLayout {
    def apply(title: String, children: Seq[ChildElement], options: FormRenderingOptions): VdomNode
  }
}
