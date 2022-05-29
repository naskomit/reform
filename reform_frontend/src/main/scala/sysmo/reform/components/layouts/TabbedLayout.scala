package sysmo.reform.components.layouts

import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent

case class NamedContent(name: String, content: VdomElement)

object TabbedLayout extends ReactComponent {
  import japgolly.scalajs.react._

  case class Props(children: Seq[NamedContent])
  case class State(active: Int)

  final class Backend($: BackendScope[Props, State]) {
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "container",
        <.div(^.className:= "container-tabbed",
          <.ul(^.className:= "container-tabs",
            p.children.zipWithIndex.map{case (nc, index) =>
              <.li(^.key:= index,
                <.a(^.href:=s"#$index",
                  ^.onClick ==> on_click(p, index),
                  (^.className:= "active").when(index == s.active),
                  nc.name)
              )
            }.toTagMod
          ),
          <.div(^.className:= "container-tab active",
            <.div(^.className:= "wrapper",p.children(s.active).content)
          )

        )
      )
    }

    def on_click(p: Props, index: Int)(event: ReactEventFromInput): Callback = $.modState{s =>
      event.preventDefault()
      s.copy(active = index)
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("TabbedLayout")
    .initialState(State(0))
    .renderBackend[Backend]
    .build

  //
  def apply(children: Seq[NamedContent]): Unmounted = {
    component(Props(children))
  }
}
