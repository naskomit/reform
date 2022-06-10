package sysmo.reform.components.forms4

import japgolly.scalajs.react.component.Js.{RawMounted, UnmountedWithRawType}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms4.transitions.ReactTransitionGroup.CSSTransition


package object transitions {
  def collapsible(in: Boolean, children: Seq[VdomNode], timeout: Int = 300): CSSTransition.Unmounted  = {
    CSSTransition(
      in, timeout, "FormCss-vert-collapse",
      <.div(children.toTagMod)
    )
  }
}
