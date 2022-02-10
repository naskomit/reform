package sysmo.coviddata.components.select

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
    trait Opt extends js.Object {
      var value: String = js.native
      var label: String = js.native
    }

    @js.native
    trait Props extends js.Object {
      var defaultValue: Opt
      var options: js.Array[Opt]
    }

    val component = JsComponent[Props, Children.None, Null](Select)

    def apply(defaultValue : EnumeratedOption, choices : Seq[EnumeratedOption]) = {
      val props = (new js.Object).asInstanceOf[Props]
      props.defaultValue = (new js.Object).asInstanceOf[Opt]
      props.defaultValue.value = defaultValue.value
      props.defaultValue.label = defaultValue.label

      val options = choices.map {x => {
        val option = (new js.Object).asInstanceOf[Opt]
        option.value =  x.value
        option.label = x.label
        option
      }}.toJSArray
      props.options = options

      component.withProps(props)()
    }
  }


}
