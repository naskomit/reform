package sysmo.coviddata

import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.coviddata.router.{Form1Page, HomePage, Pages}

object IntuitiveLayout {
  import japgolly.scalajs.react._
  case class State(menu_expanded: Boolean = true)
  case class Props(router: RouterCtl[Pages], resolution: Resolution[Pages])

  final class Backend($: BackendScope[Props, State]) {
    def header(router: RouterCtl[Pages]) = {
      <.div(^.className:="dev-page-header",
        <.div(^.className:="dph-logo",
          router.link(HomePage)("Home"),
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

    def sidebar(router: RouterCtl[Pages]) = {
      <.div(^.className:="dev-page-sidebar",
        <.ul(^.className:="dev-page-navigation",
          <.li(^.className:="title", "Navigation"),
          <.li(router.link(HomePage)("Home", <.i(^.className := "fa fa-desktop"))),
          <.li(router.link(Form1Page)("Form 1", <.i(^.className := "fa fa-pencil")))
        ))
    }

    def content(resolution: Resolution[Pages]) = {
      <.div(^.className:="dev-page-content",
        <.div(^.className:= "container",resolution.render()))
    }

    def render (p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.className:=(if (s.menu_expanded) "dev-page dev-page-loaded" else "dev-page dev-page-loaded dev-page-sidebar-collapsed"),
          header(p.router),
          <.div(^.className:="dev-page-container", sidebar(p.router), content(p.resolution))),
        footer()
      )
    }

  }

  val component = ScalaComponent.builder[Props]("Intuitive Layout")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(p : Props) = component(p)

}
