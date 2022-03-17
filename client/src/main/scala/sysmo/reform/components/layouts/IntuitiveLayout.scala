package sysmo.reform.components.layouts

import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.router.{Page, PageCollection}

object IntuitiveLayout extends ApplicationLayout {
  import japgolly.scalajs.react._
  case class State(menu_expanded: Boolean = true)
  case class Props(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection)

  final class Backend($: BackendScope[Props, State]) {
    def header(p: Props) = {
      <.div(^.className:="dev-page-header",
        <.div(^.className:="dph-logo",
          p.router.link(p.pages.home)(p.pages.home.name),
          <.a(^.className:="dev-page-sidebar-collapse",
            <.div(^.className:="dev-page-sidebar-collapse-icon",
              ^.onClick --> $.modState(s => State(!s.menu_expanded)),
              <.span(^.className:="line-one"),
              <.span(^.className:="line-two"),
              <.span(^.className:="line-three")
            ))))
    }

    def footer() = {
      <.div(^.className:="dev-page-footer dev-page-footer-fixed dev-page-footer-effect",
        "Footer")
    }

    def sidebar(p: Props) = {
      val links : TagMod = p.pages.html(
        x => <.li(p.router.link(x)(x.label, <.i(^.className := x.icon)))
      )(
        (x, children) => <.div("Unimplemented")
      )

      <.div(^.className:="dev-page-sidebar",
        <.ul(^.className:="dev-page-navigation",
          <.li(^.className:="title", "Navigation"), links
        ))
    }

    def content(resolution: Resolution[Page]) = {
      <.div(^.className:="dev-page-content",
        <.div(^.className:= "container",resolution.render()))
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.className:=(if (s.menu_expanded) "dev-page dev-page-loaded" else "dev-page dev-page-loaded dev-page-sidebar-collapsed"),
          header(p),
          <.div(^.className:="dev-page-container", sidebar(p), content(p.resolution))),
        footer()
      )
    }

  }

  val component = ScalaComponent.builder[Props]("Intuitive Layout")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(router: RouterCtl[Page], resolution: Resolution[Page], pages: PageCollection) =
    component(Props(router, resolution, pages))

}
