package sysmo.reform.components.select

import japgolly.scalajs.react.{Children, JsComponent}
import sysmo.reform.shared.data.EnumeratedOption

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import js.JSConverters._

object ReactSelectFacades {

  object ReactSelectNativeComponent {
    @JSImport("react-select", JSImport.Default)
    @js.native
    object Select extends js.Object

    @js.native
    trait Choice extends js.Object {
      var value: String = js.native
      var label: String = js.native
    }

    type OnInputChange = js.Function2[String, Any, Unit]
    type OnMenuOpen = js.Function0[Unit]
    @js.native
    trait Props extends js.Object {
      var defaultValue: js.UndefOr[Choice]
      var isDisabled: js.UndefOr[Boolean]
      var isLoading: js.UndefOr[Boolean]
      var isClearable: js.UndefOr[Boolean]
      var isSearchable: js.UndefOr[Boolean]
      var isMulti: js.UndefOr[Boolean]
      var onInputChange: js.UndefOr[OnInputChange]
      var onMenuOpen: js.UndefOr[OnMenuOpen]
      var options: js.Array[Choice]
    }

    val component = JsComponent[Props, Children.None, Null](Select)

    def apply(defaultValue : Option[EnumeratedOption], options : Seq[EnumeratedOption],
              is_disabled: Option[Boolean] = None, is_loading: Option[Boolean] = None,
              is_clearable: Option[Boolean] = None, is_searchable: Option[Boolean] = None,
              on_input_changed: Option[OnInputChange] = None,
              on_menu_open: Option[OnMenuOpen] = None) = {
      val props = (new js.Object).asInstanceOf[Props]

      props.defaultValue = defaultValue match {
        case Some(p) => {
          val dv = (new js.Object).asInstanceOf[Choice]
          dv.value = p.value
          dv.label = p.label
          dv
        }
        case None => js.undefined
      }

      props.options = options.map { x => {
        val option = (new js.Object).asInstanceOf[Choice]
        option.value =  x.value
        option.label = x.label
        option
      }}.toJSArray

      props.isDisabled = is_disabled.orUndefined
      props.isClearable = is_clearable.orUndefined
      props.isLoading = is_loading.orUndefined
      props.isSearchable = is_searchable.orUndefined
      props.onInputChange = on_input_changed.orUndefined
      props.onMenuOpen = on_menu_open.orUndefined
      component.withProps(props)()
    }
  }


}
