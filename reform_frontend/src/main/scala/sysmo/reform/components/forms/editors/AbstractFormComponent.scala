package sysmo.reform.components.forms.editors

import japgolly.scalajs.react.CtorType
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.VdomElement
import sysmo.reform.components.ReactComponent
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.form.{runtime => FR}

trait AbstractFormComponent[FRT <: FR.RuntimeObject, FBT <: FB.FormElement] extends ReactComponent {
  case class Props(obj: FRT, options: FormRenderingOptions)
  type Partial = FormRenderingOptions => VdomElement
  type FieldBuildType = FBT
  type RuntimeObjT = FRT
  def partial(field_obj: FRT): Partial = {
    (options) => apply(field_obj, options)
  }
  val component: Component[Props, State, Backend, CtorType.Props]
  def apply(field_obj: FRT, options: FormRenderingOptions): Unmounted =
    component.apply(Props(field_obj, options))

}
