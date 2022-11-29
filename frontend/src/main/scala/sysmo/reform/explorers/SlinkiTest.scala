package sysmo.reform.explorers
import org.scalajs.dom
import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js

object SlinkiTest extends StatelessComponentWrapper {
  case class Props()

//  private val css = AppCSS

  // @ScalaJSDefined
  class Def(js_props: js.Object) extends Definition(js_props) {
    def on_click(): Unit = dom.console.log("Clicked")
    def render(): ReactElement = {
      div(
        button("Hello", onClick := on_click _)
      )
    }
  }

  def apply() = super.apply(Props())
}