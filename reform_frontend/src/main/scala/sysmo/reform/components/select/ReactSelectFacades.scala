package sysmo.reform.components.select

import japgolly.scalajs.react.{Children, JsComponent}
import sysmo.reform.shared.util.LabeledValue
import sysmo.reform.shared.{form => F}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import js.JSConverters._
import scala.scalajs.js.|

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

    type OneOrMoreSelected = Choice | js.Array[Choice]

    @js.native
    trait ActionMeta extends js.Object {
      val action: String
      val prevInputValue: String
    }

    type OnInputChange = js.Function2[String, ActionMeta, Unit]
    type OnChange = js.Function2[OneOrMoreSelected, ActionMeta, Unit]
    type OnMenuOpen = js.Function0[Unit]
    @js.native
    trait Props extends js.Object {
      var value: js.UndefOr[OneOrMoreSelected]
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

    class Builder {
      val props = (new js.Object).asInstanceOf[Props]

      def value(v: F.FieldValue[_]): this.type = {
        props.value = v match {
          case F.SomeValue(x) => {
            val dv = (new js.Object).asInstanceOf[Choice]
            dv.value = x.value
            dv.label = x.make_label
            dv
          }

          case F.MultiValue(s) => {
            s.map { x =>
              val dv = (new js.Object).asInstanceOf[Choice]
              dv.value = x.value
              dv.label = x.make_label
              dv
            }.toJSArray
          }

          case F.NoValue => null


        }
        this
      }

      def options(v: Seq[LabeledValue[_]]): this.type = {
        props.options = v.map { x => {
          val option = (new js.Object).asInstanceOf[Choice]
          option.value =  x.value
          option.label = x.make_label
          option
        }}.toJSArray
        this
      }

      def map(f: this.type => Unit): this.type = {
        f(this)
        this
      }

      def multiple(): this.type = {
        props.isMulti = Some(true).orUndefined
        this
      }

      //              is_disabled: Option[Boolean] = None, is_loading: Option[Boolean] = None,
      //              is_clearable: Option[Boolean] = None, is_searchable: Option[Boolean] = None,
      /** Callbacks */
      def on_change(f: OnChange): this.type = {
        props.onChange = f
        this
      }

      def on_input_change(f: OnInputChange): this.type = {
        props.onInputChange = f
        this
      }

      def on_menu_open(f: OnMenuOpen): this.type = {
        props.onMenuOpen = f
        this
      }

      def build = component.withProps(props)()
    }

    def builder: Builder = new Builder

  }


}
