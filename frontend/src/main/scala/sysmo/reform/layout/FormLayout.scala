package sysmo.reform.layout

import japgolly.scalajs.react.vdom.VdomNode
import sysmo.reform.react.ReactComponent
import sysmo.reform.react.property.PropertySourceView

case class FormItem(label: String, editor: VdomNode)

trait FormLayout extends ReactComponent {
  case class Props(items: Seq[FormItem])


  def apply(items: Seq[FormItem]): Unmounted = component(Props(items))
}

