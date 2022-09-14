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
      val rowGap = VdomStyle("rowGap")
      <.div(^.display:= "flex", ^.flexWrap:= "wrap",
        ^.columnGap:= 50.px, rowGap:= 50.px,
        p.items.map(item =>
          <.div(
            ^.minWidth := 200.px,
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
