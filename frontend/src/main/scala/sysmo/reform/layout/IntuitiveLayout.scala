package sysmo.reform.layout

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.app.{Layout, Page, PageCollection, PageBase}


object IntuitiveLayout extends Layout {
  case class State
  (
    menu_expanded: Boolean = false,
    active_item: String = "Root"
  )
  case class Props(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection, elem_height: Int = 50)

  final class Backend($: BackendScope[Props, State]) {
    def header(p: Props): VdomElement = {
      <.div(^.className:="dev-page-header",
        //<.a(^.href:= p.router.urlFor(p.pages.home).value,
        <.div(^.className:="dph-logo",
          <.a(^.href:= p.router.urlFor(p.pages.home).value),
          <.a(^.className:="dev-page-sidebar-collapse",
            <.div(^.className:="dev-page-sidebar-collapse-icon",
              ^.onClick --> $.modState(s => s.copy(menu_expanded = !s.menu_expanded)),
              <.span(^.className:="line-one"),
              <.span(^.className:="line-two"),
              <.span(^.className:="line-three")
            ))))
    }

    def footer(): VdomElement = {
      <.div(^.className:="dev-page-footer dev-page-footer-fixed dev-page-footer-effect",
        "Footer")
    }

    def panel_link(p: Props, x: Page): VdomElement = {
      <.li(p.router.link(x)(<.i(^.className := x.icon), x.make_label))
    }

    def click_handler(x: PageBase, leaf: Boolean)(e: ReactEventFromInput): Callback = Callback {
      if (!leaf) e.preventDefault()
    } >> $.modState(s => {
      if (s.active_item.startsWith(x.path)) {
        val active_item = x.path.split("\\.").dropRight(1).mkString(".")
        s.copy(active_item = active_item)
      } else {
        s.copy(active_item = x.path)
      }

    })

    def sidebar(p: Props, s: State): VdomElement = {
      val links : TagMod = p.pages.html(
        x => panel_link(p, x)
      )(
        (x, child_tag_mod) => {
          val is_active = s.active_item.startsWith(x.path)
          <.li(^.classSet("has-child" -> true, "active" -> is_active),
            <.a(^.href := "#", ^.onClick==> click_handler(x, leaf = false),
              <.span(x.make_label)),
            <.ul(^.maxHeight:= (if (is_active) x.children.length * p.elem_height else 0).toString + "px", child_tag_mod)
          )
        }
      )

      <.div(^.className:="dev-page-sidebar",
        <.ul(^.className:="dev-page-navigation",
          <.li(^.className:="title", "Navigation"), links
        ))
    }

    def content(resolution: Resolution[Page]): VdomElement = {
      <.div(^.className:="dev-page-content",
        <.div(^.className:= "container",resolution.render()))
    }

    def render (p: Props, s: State): VdomElement = {
//      println("Rendered")
//      println(s)
      val height = dom.window.innerHeight - 100
      <.div(
        <.div(^.className:=(if (s.menu_expanded) "dev-page dev-page-loaded" else "dev-page dev-page-loaded dev-page-sidebar-collapsed"),
          header(p),
          <.div(^.className:="dev-page-container", ^.height:= height.px, ^.marginBottom:= 0.px,
            sidebar(p, s), content(p.resolution))),
        footer()
      )
    }

  }

  val component = ScalaComponent.builder[Props]("Intuitive Layout")
    .initialStateFromProps(p => {
//      val item_expanded = p.pages.collect.map(x => (x.path, false)).toMap
//      println(p.pages.collect.map(x => (x.name, x.path)))
//      item_expanded = item_expanded
      State()
    })
    .renderBackend[Backend]
    .build

  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection) =
    component(Props(router, resolution, pages))

}
