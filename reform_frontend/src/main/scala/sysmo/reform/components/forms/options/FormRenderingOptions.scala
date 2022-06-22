package sysmo.reform.components.forms.options

import sysmo.reform.components.forms.layouts.{ColumnsLayoutComponent, FormGroupLayout, ArrayGroupLayout,RowLayoutComponent}
import sysmo.reform.shared.util.{Property, PropertyHolder}

object FormRenderingProps {
  val background_white = Property[Boolean]("background", true)
  val depth = Property[Int]("level", 0)
  val form_group_layout = Property[FormGroupLayout](
    name = "form_group_layout", ColumnsLayoutComponent
  )
  val group_array_layout = Property[ArrayGroupLayout](
    name = "group_array_layout", RowLayoutComponent
  )

}

class FormRenderingOptions extends PropertyHolder[FormRenderingOptions] {
  def fork: Concrete = new FormRenderingOptions
  override type Props = FormRenderingProps.type
  override val props = FormRenderingProps
}

object FormRenderingOptions {
  def default: FormRenderingOptions = new FormRenderingOptions
}