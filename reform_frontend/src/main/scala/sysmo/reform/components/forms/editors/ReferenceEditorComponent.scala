package sysmo.reform.components.forms.editors

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sysmo.reform.components.select.ReactSelectFacades.{ReactSelectNativeComponent => RSNC}
import sysmo.reform.shared.form.{build => FB, runtime => FR}
import sysmo.reform.shared.util.LabeledValue

import scala.scalajs.js

object ReferenceEditorComponent extends AbstractFormComponent[FR.Reference, FB.Reference] {
  type FieldValueType = FR.FieldValue[FR.Reference]
  case class State(choices: Seq[LabeledValue[_]], is_loading: Boolean)
  implicit class PropOps(p: Props) {
    def value: FieldValueType = p.obj.ref_id.asInstanceOf[FieldValueType]
    def field: FieldBuildType = p.obj.prototype.asInstanceOf[FieldBuildType]
  }

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val value = p.obj.runtime.label_ref(p.obj).ref_id
      RSNC.builder
        .value(value).options(s.choices)
        .map(b => if (p.field.multiple) b.multiple())
        .on_change(on_change(p, s))
        .on_input_change(on_input_change(p, s))
        .on_menu_open(on_menu_open(p, s))
        .build
    }

    def label_value(x: LabeledValue[_], s: State): LabeledValue[_] = x match {
      case LabeledValue(value, Some(label)) => x
      case LabeledValue(value, None) if s.choices.nonEmpty => s.choices.find(_.value == value).getOrElse(x)
      case _ => x
    }

    def load_choices(p: Props, s: State): AsyncCallback[Unit] = {
      $.modState(s => s.copy(is_loading = true)).async >>
        AsyncCallback.fromFuture(p.obj.runtime.get_ref_choices(p.obj.id)) >>=
        {choices => $.modState(s => s.copy(choices = choices, is_loading = false)).async}
    }
    def find_choice(v: Any, choices: Seq[LabeledValue[_]]): Option[LabeledValue[_]] =
      choices.find(c => c.value == v)

    def update_selection(p: Props, s: State)(selection: RSNC.OneOrMoreSelected): Unit = {
      selection match {
        case many: js.Array[RSNC.Choice] => {
          val original_choice: Seq[LabeledValue[_]] = many.map(x => find_choice(x.value, s.choices)).collect {
            case Some(x) => x
          }.toSeq
          p.obj.runtime.dispatch(
            FR.SetFieldValue(p.obj.id, FR.MultiValue(original_choice))
          )
        }
        case one => {
          find_choice(one.asInstanceOf[RSNC.Choice].value, s.choices) match {
            case Some(x) => p.obj.runtime.dispatch(
              FR.SetFieldValue(p.obj.id, FR.SomeValue(x))
            )
            case None =>
          }
        }
      }
    }

    def on_change(p: Props, s: State): RSNC.OnChange = (selection: RSNC.OneOrMoreSelected, action_meta: RSNC.ActionMeta) => {
//      org.scalajs.dom.console.log(selection)
//      org.scalajs.dom.console.log(action_meta.action)
      //logger.info(s"[on_change]: ${action_meta.action} / ${choice.value} : ${choice.label}")

      action_meta.action match {
        case "select-option" => update_selection(p, s)(selection)
        case "deselect-option" => throw new IllegalArgumentException("deselect-option")
        case "remove-value" => update_selection(p, s)(selection)
        case "pop-value" => throw new IllegalArgumentException("pop-value")
        case "clear" => p.obj.runtime.dispatch(FR.SetFieldValue(p.obj.id, FR.NoValue))
        case "create-option" => throw new IllegalArgumentException("create-option")
      }
    }

    def on_input_change(p: Props, s: State): RSNC.OnInputChange = (value: String, action_meta: RSNC.ActionMeta) => {
//      logger.info(value)
//      logger.info(action_meta.action)
    }

    def on_menu_open(p: Props, s: State): RSNC.OnMenuOpen = () => load_choices(p, s).runNow()
  }


  //  // TODO
  //  implicit def FieldValue_reuse[A]: Reusability[FieldValue]  = Reusability.by_==
  //  implicit val props_reuse = Reusability.by((_ : Props).value)
  //  implicit val state_reuse = Reusability.derive[State]

  val component =
    ScalaComponent.builder[Props]("SelectEditor")
      .initialState(State(Seq(), is_loading = true))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.load_choices(f.props, f.state))
//            .configure(Reusability.shouldComponentUpdate)
      .build

//  def apply(field_obj: FR.AtomicValue[FB.SelectField], options: FormRenderingOptions): Unmounted = {
//    component(Props(field_obj, options))
//  }

}
