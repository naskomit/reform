package sysmo.reform.components.forms4.layouts

import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^._

object Utils {
  case class CollapsibleEffect(expanded: Boolean, toggle: Callback)

  def make_title_collapsible(title: Option[String], depth: Int, collapsible: CollapsibleEffect): Option[VdomNode] =
    title.map{title =>
      val header_fn = (depth + 1) match {
        case 1 => <.h1
        case 2 => <.h2
        case 3 => <.h3
        case 4 => <.h4
        case 5 => <.h5
        case 6 => <.h6
      }

      val chevron = <.i(
        ^.classSet1("fa",
          "fa-chevron-down" -> collapsible.expanded,
          "fa-chevron-right" -> !collapsible.expanded),
        ^.fontSize := "0.6em",
        ^.verticalAlign := "20%",
        ^.onClick --> collapsible.toggle
      )

      header_fn(chevron, " ", title)

    }

}
