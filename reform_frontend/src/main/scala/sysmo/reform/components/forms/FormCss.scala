package sysmo.reform.components.forms

import sysmo.reform.css.CssSettings._

object FormCss extends StyleSheet.Inline {
  import dsl._
  /** Global styles */
  style("container .wrapper")(
    border(1.px, solid, Color("#DDD"))
  )

  style("container .wrapper-dark")(
    background:= "#EEF1F7"
  )

  /** Collapsible styles */

  val `vert-collapse-enter` = style(
    opacity(0),
    transition:= "opacity 300ms"
  )

  val `vert-collapse-enter-active` = style(
    opacity(1)
  )

  val `vert-collapse-exit` = style(
    opacity(1)
  )

  val `vert-collapse-exit-active` = style(
    opacity(0),
    transition:= "opacity 300ms"
  )

  val `vert-collapse-exit-done` = style(
    opacity(0),
    maxHeight(0.px),
    overflow.hidden,
  )

}