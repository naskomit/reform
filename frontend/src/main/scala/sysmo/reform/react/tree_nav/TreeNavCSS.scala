package sysmo.reform.react.tree_nav

import sysmo.reform.css.CssSettings._

class TreeNavCSS(implicit r: StyleSheet.Register) extends StyleSheet.Inline()(r) {
  import dsl._

  /** Tree navigator */
  val nav = style(

  )

  val nav_item = style(
    cursor.pointer,
  )

  val row = style(
    display.flex,
    flexWrap.nowrap,
    &.hover(
      backgroundColor(grey(220))
    )
  )

  val row_selected = style(
    backgroundColor(c"#e0eff5"),
    borderTop(1.px, solid, c"#c4e0eb"),
    borderBottom(1.px, solid, c"#c4e0eb")
  )

  val item_expand = style(
    display.flex,
    justifyContent.center,
    alignItems.center,
    height(11.px),
    width(11.px),
    border(1.px, solid, grey(200))
  )

  val item_expand_leaf = style(
    item_expand,
    borderColor(transparent)
  )

  val item_icon = style(
    marginLeft(10.px),
    marginRight(10.px),
    display.inline
  )

  val item_name = style(
    display.inline
  )

  val item_context_menu = style(
    marginLeft(auto),
    marginRight(10.px),
    padding(3.px),
    borderWidth(1.px),
    borderStyle(solid),
    borderColor(transparent),
    &.hover(
      borderColor(grey(100))
    )
  )

  val context_menu_container = style(
    position.absolute,
//    float.left,
    border.solid,
    borderWidth(1.px),
    borderColor(c"#e3e6ec"),
    boxShadow:= "0 2px 10px rgb(0 0 0 / 10%)",
    width.auto,
    lineHeight(15.px),
    whiteSpace.nowrap,
    backgroundColor(white),
    opacity(1),
    zIndex(1000)
//    top(0.px),
//    left(0.px)
  )

  val context_menu_item = style(
    padding(10.px),
    &.hover(
      backgroundColor(grey(220))
    )
  )


  //  style("tree-item-row:hover .tree-item-context-menu")(
  //    display.block
  //  )


  val item_children = style(
    display.block,
    borderLeftWidth(1.px),
    borderLeftStyle.solid,
    borderLeftColor.grey(200)
  )
}
