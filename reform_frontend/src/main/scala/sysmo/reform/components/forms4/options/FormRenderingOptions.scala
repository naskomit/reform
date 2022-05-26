package sysmo.reform.components.forms4.options

import sysmo.reform.components.forms4.layouts.{ColumnsLayout, FormGroupLayout}
import sysmo.reform.shared.util.{Property, PropertyHolder}

object FormRenderingProps {
  val background = Property[Boolean]("background", false)
  val depth = Property[Int]("level", 0)
  val form_group_layout = Property[FormGroupLayout](name = "form_group_layout", ColumnsLayout(3))
}

class FormRenderingOptions extends PropertyHolder[FormRenderingOptions] {
  def fork: Concrete = new FormRenderingOptions
  override type Props = FormRenderingProps.type
  override val props = FormRenderingProps
}

object FormRenderingOptions {
  def default: FormRenderingOptions = new FormRenderingOptions
}