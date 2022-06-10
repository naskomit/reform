package sysmo.reform.components.forms4.transitions

import japgolly.scalajs.react.component.Js.{RawMounted, UnmountedWithRawType}
import japgolly.scalajs.react.vdom.VdomNode

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react.{Children, JsComponent}
import sysmo.reform.components.forms4.transitions.ReactTransitionGroupFacades.CSSTransition

import js.JSConverters._
import scala.scalajs.js.|

object ReactTransitionGroupFacades {
  object CSSTransition {
    @JSImport("react-transition-group", "CSSTransition")
    @js.native
    object Component extends js.Object

    @js.native
    trait Props extends js.Object {
      var in: Boolean
      var timeout: Int
      var classNames: String
    }

    val component = JsComponent[Props, Children.Varargs, Null](Component)
  }

  object TransitionGroup {
    @JSImport("react-transition-group", "TransitionGroup")
    @js.native
    object Component extends js.Object

    @js.native
    trait Props extends js.Object {
//      var in: Boolean
//      var timeout: Int
    }

    val component = JsComponent[Props, Children.Varargs, Null](Component)

  }

}

object ReactTransitionGroup {
  val Native = ReactTransitionGroupFacades
  object CSSTransition {
    type Unmounted = UnmountedWithRawType[Native.CSSTransition.Props, Null, RawMounted[Native.CSSTransition.Props, Null]]
    def apply(in: Boolean, timeout: Int, class_names: String, child: VdomNode): Unmounted = {
      val p = (new js.Object()).asInstanceOf[Native.CSSTransition.Props]
      p.in = in
      p.timeout = timeout
      p.classNames = class_names
      Native.CSSTransition.component(p)(child)
    }
  }

  object TransitionGroup {
    def apply(in: Boolean,children: Seq[VdomNode]) = {
      val p = (new js.Object()).asInstanceOf[Native.TransitionGroup.Props]
      Native.TransitionGroup.component(p)(children: _*)
    }

  }
}