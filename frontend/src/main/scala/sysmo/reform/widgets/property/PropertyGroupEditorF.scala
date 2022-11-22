package sysmo.reform.widgets.property

import cats.MonadThrow
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.widgets.ReactComponent
import sysmo.reform.shared.sources.property.{FieldProperty, LocalPropertySource, PropertySource}
import sysmo.reform.effects.implicits._
import sysmo.reform.layout.form.{FlexFormLayout, FormItem, FormLayout}
import sysmo.reform.shared.runtime.{RecordFieldInstance, RuntimeAction, SetFieldValue}
import sysmo.reform.shared.types.{ArrayType, MultiReferenceType, PrimitiveDataType, RecordType, ReferenceType}

case class PropertySourceView()
case class PropertyView()

class PropertyGroupEditorF[F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  object StringEditorComponent extends StringEditorComponentF[F]
  object IntegerEditorComponent extends IntegerEditorComponentF[F]
  object FloatEditorComponent extends FloatEditorComponentF[F]

  case class Props(ps: PropertySource[F], layout: FormLayout)
  case class State(ps_local: Option[LocalPropertySource[F]])
  class Backend($: BScope) {
    def render(props: Props, state: State): VdomElement = {

      state.ps_local match {
        case Some(ps) => props.layout(ps.props_sync.map {
          case prop@FieldProperty(field) => {
            val editor: VdomElement = field.ftype.dtype match {
              case dt: PrimitiveDataType => {
                dt match {
                  case PrimitiveDataType.Real => FloatEditorComponent(
                    ps.id, field, props.ps.dispatcher.tap(on_prop_update(props))
                  )
                  case PrimitiveDataType.Int => IntegerEditorComponent(
                    ps.id, field, props.ps.dispatcher.tap(on_prop_update(props))
                  )
                  case PrimitiveDataType.Long => IntegerEditorComponent(
                    ps.id, field, props.ps.dispatcher.tap(on_prop_update(props))
                  )
                  case PrimitiveDataType.Char => StringEditorComponent(
                    ps.id, field, props.ps.dispatcher.tap(on_prop_update(props))
                  )
                  case PrimitiveDataType.Bool => ???
                  case PrimitiveDataType.Date => ???
                  case PrimitiveDataType.Id => ???
                }
              }
              case recordType: RecordType => <.div(s"Record[${recordType.symbol}]")
              case arrayType: ArrayType => <.div(s"Array[${arrayType.prototype.symbol}]")
              case referenceType: ReferenceType => ???
              case referenceType: MultiReferenceType => ???
            }
            FormItem(prop.descr, editor)
          }

        }.toSeq)
        case None => <.div("Loading properties")
      }
    }

    def on_prop_update(props: Props)(action: RuntimeAction): F[Unit] = {
      action match {
        case SetFieldValue(_, _) => load_props(props).runNow()
        case _ =>
      }
      f2c.mt.pure()
    }

    def load_props(props: Props): AsyncCallback[Unit] = {
      for {
        p_seq <- f2c.async(props.ps.cache)
        _ <- $.modStateAsync(s => s.copy(ps_local = Some(p_seq)))
      } yield ()
    }
  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
      .initialState(State(None))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.load_props(f.props))
      .build

  def apply(ps: PropertySource[F]): Unmounted =
    component(Props(ps, FlexFormLayout))
}
