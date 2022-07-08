package sysmo.reform.components.forms.layouts

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.forms.options.FormRenderingOptions
import sysmo.reform.components.forms.{transitions => Tr}

import scala.collection.mutable

object ColumnsLayoutComponent extends FormGroupLayout {
  case class Props(child_elements: Seq[GroupChildNode], options: FormRenderingOptions)
  case class State(expanded: Boolean)

  class Builder(options: FormRenderingOptions) {
    private val row_nodes: mutable.ArrayBuffer[VdomNode] = mutable.ArrayBuffer()
    private var row_filled_width = 0.0
    private var row_white: Boolean = options.get(_.background_white)
    private val grid: mutable.ArrayBuffer[VdomElement] = mutable.ArrayBuffer()
    private var row_index = 0
    private var elem_index = 0
    private val base_col_width = 0.33

    def compute_width(elem: GroupChildNode): Double  = {
      val size = elem.size_hint match {
        case ExtraShort => base_col_width * 0.25
        case Short => base_col_width * 0.5
        case Medium => base_col_width
        case Long => base_col_width * 1.5
        case ExtraLong => base_col_width * 2
        case FullWidth => 0.95

      }
      Math.max(size, 0.1)
    }

    def build_row(): Unit = {
      val key = row_index.toString
      grid += <.div(^.className:= f"row $key", ^.key:= key, row_nodes.toTagMod)
      row_index += 1
      elem_index = 0
      row_filled_width = 0.0
      row_nodes.clear()
    }


    def build_content(child_elements: Seq[GroupChildNode]): Seq[VdomElement] = {
      for (elem <- child_elements) {
        val elem_width = compute_width(elem)
        val dom_element = if (elem.size_hint == FullWidth) {
          build_row()
          row_white = !row_white
          val child_options = elem.options.update(_.background_white:= row_white)

          <.div(^.classSet1("wrapper",
              "wrapper-white" -> row_white,
              "wrapper-dark" -> !row_white,
            ),
            CollapsibleSection(
              elem.title, elem.options.get(_.depth) + 1, Seq(elem.node(child_options))
            )
          )
        } else {
          if (row_filled_width + elem_width > 1.0)
            build_row()

          <.div(
            ^.className:= f"col-md-${Math.round(elem_width * 12)} $elem_index",
            ^.key:= elem_index,
            <.div(^.className := "form-group",
              <.label(elem.title),
              elem.node(elem.options)
            )
          )
        }

        row_nodes += dom_element
        row_filled_width += elem_width
        elem_index += 1
      }

      if (row_nodes.nonEmpty) {
        build_row()
      }
      grid.toSeq
    }
  }

  class Backend($: BackendScope[Props, State]) {


    def render(p: Props, s: State) : VdomNode = {

      <.div(
        (new Builder(p.options)).build_content(p.child_elements).toTagMod
      )
    }



  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
    .initialState(State(true))
    .renderBackend[Backend]
//    .render_PCS((p, c, s) => (new Backend).render(p, c, s))
    .build

  override def apply(children: Seq[GroupChildNode], options: FormRenderingOptions): Unmounted =
    component(Props(children, options))

}


