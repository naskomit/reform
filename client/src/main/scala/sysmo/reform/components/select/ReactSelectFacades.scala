package sysmo.reform.components.select

import japgolly.scalajs.react.{Children, JsComponent}
import org.scalajs.dom
import sysmo.reform.shared.util.LabeledValue

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
      var value: Any = js.native
      var label: String = js.native
    }

    @js.native
    trait ActionMeta extends js.Object {
      val action: String
      val prevInputValue: String
    }

    type OnInputChange = js.Function2[String, ActionMeta, Unit]
    type OnChange = js.Function2[Choice, ActionMeta, Unit]
    type OnMenuOpen = js.Function0[Unit]
    @js.native
    trait Props extends js.Object {
      var value: js.UndefOr[Choice]
      var defaultValue: js.UndefOr[Choice]
      var isDisabled: js.UndefOr[Boolean]
      var isLoading: js.UndefOr[Boolean]
      var isClearable: js.UndefOr[Boolean]
      var isSearchable: js.UndefOr[Boolean]
      var isMulti: js.UndefOr[Boolean]
      var onChange: js.UndefOr[OnChange]
      var onInputChange: js.UndefOr[OnInputChange]
      var onMenuOpen: js.UndefOr[OnMenuOpen]
      var options: js.Array[Choice]
    }

    val component = JsComponent[Props, Children.None, Null](Select)

    def apply(value: Option[LabeledValue[_]], options : Seq[LabeledValue[_]],
              defaultValue : Option[LabeledValue[_]] = None,
              is_disabled: Option[Boolean] = None, is_loading: Option[Boolean] = None,
              is_clearable: Option[Boolean] = None, is_searchable: Option[Boolean] = None,
              on_change: Option[OnChange] = None, on_input_change: Option[OnInputChange] = None,
              on_menu_open: Option[OnMenuOpen] = None) = {
      val props = (new js.Object).asInstanceOf[Props]


      props.value = value match {
        case Some(p) => {
          val dv = (new js.Object).asInstanceOf[Choice]
          dv.value = p.value
          dv.label = p.make_label
          dv
        }
        case None => null
      }

      props.defaultValue = defaultValue match {
        case Some(p) => {
          val dv = (new js.Object).asInstanceOf[Choice]
          dv.value = p.value
          dv.label = p.make_label
          dv
        }
        case None => js.undefined
      }

      props.options = options.map { x => {
        val option = (new js.Object).asInstanceOf[Choice]
        option.value =  x.value
        option.label = x.make_label
        option
      }}.toJSArray

      props.isDisabled = is_disabled.orUndefined
      props.isClearable = is_clearable.orUndefined
      props.isLoading = is_loading.orUndefined
      props.isSearchable = is_searchable.orUndefined
      props.onChange = on_change.orUndefined
      props.onInputChange = on_input_change.orUndefined
      props.onMenuOpen = on_menu_open.orUndefined
      component.withProps(props)()
    }
  }


}
