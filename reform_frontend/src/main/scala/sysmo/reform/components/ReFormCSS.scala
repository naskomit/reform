package sysmo.reform.components

import sysmo.reform.css.CssSettings._
import sysmo.reform.components.tree_nav.TreeNavCSS

object ReFormCSS extends StyleSheet.Inline {
  import dsl._
  /** Global styles */
  //  style("container .wrapper")(
  //    border(1.px, solid, Color("#DDD"))
  //  )

  style("container .wrapper-neutral")(
    background:= "#EEF1F7"
  )

  style("container .wrapper-dark")(
    background:= "#DDE3EF"
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

  val tree_nav = new TreeNavCSS
}