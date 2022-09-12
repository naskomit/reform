package sysmo.reform.layout
import japgolly.scalajs.react.CtorType
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object FlexFormLayout extends FormLayout {
  case class State()
  class Backend($: BScope) {
    def render(p: Props, s: State): VdomNode = {
      <.div(^.display:= "flex", ^.flexWrap:= "true",
        ^.columnGap:= 50.px,
        p.items.map(item =>
          <.div(^.className:= "form-group",
            <.label(item.label),
            <.div(item.editor)
          )
        ).toTagMod
      )
    }
  }

  override val component = ScalaComponent.builder[Props]("FlexFormLayout")
    .initialState(State())
    .renderBackend[Backend]
    .build
}
