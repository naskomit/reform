package sysmo.reform.layout.form

import japgolly.scalajs.react.vdom.VdomNode
import sysmo.reform.widgets.ReactComponent

case class FormItem(label: String, editor: VdomNode)

trait FormLayout extends ReactComponent {
  case class Props(items: Seq[FormItem])


  def apply(items: Seq[FormItem]): Unmounted = component(Props(items))
}

