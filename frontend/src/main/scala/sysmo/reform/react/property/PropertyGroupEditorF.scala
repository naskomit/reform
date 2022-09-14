package sysmo.reform.react.property

import cats.MonadThrow
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.sources.property.{LocalPropertySource, PropertySource}
import sysmo.reform.layout.{FlexFormLayout, FormItem, FormLayout}
import sysmo.reform.effects.implicits._
import sysmo.reform.shared.runtime.{RuntimeAction, SetValue}
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, MultiReferenceType, RecordType, ReferenceType}

case class PropertySourceView()
case class PropertyView()

class PropertyGroupEditorF[F[+_]](implicit f2c: F2Callback[F], mt: MonadThrow[F]) extends ReactComponent {
  object StringEditorComponent extends StringEditorComponentF[F]
  object IntegerEditorComponent extends IntegerEditorComponentF[F]

  case class Props(ps: PropertySource[F], layout: FormLayout)
  case class State(ps_local: Option[LocalPropertySource[F]])
  class Backend($: BScope) {
    def render(props: Props, state: State): VdomElement = {

      state.ps_local match {
        case Some(ps) => props.layout(ps.props_sync.map {x =>
          val editor: VdomElement = x.dtype match {
            case dataType: AtomicDataType => dataType match {
              case AtomicDataType.Real => ???
              case AtomicDataType.Int => IntegerEditorComponent(
                x.id, x.value, props.ps.dispatcher.tap(on_prop_update(props))
              )
              case AtomicDataType.Long => IntegerEditorComponent(
                x.id, x.value, props.ps.dispatcher.tap(on_prop_update(props))
              )
              case AtomicDataType.Char => StringEditorComponent(
                x.id, x.value, props.ps.dispatcher.tap(on_prop_update(props))
              )
              case AtomicDataType.Bool => ???
              case AtomicDataType.Date => ???
              case AtomicDataType.Id => ???
            }
            case recordType: RecordType => <.div(s"Record[${recordType.symbol}]")
            case arrayType: ArrayType => <.div(s"Array[${arrayType.prototype.symbol}]")
            case referenceType: ReferenceType => ???
            case referenceType: MultiReferenceType => ???
          }
          FormItem(x.descr, editor)
        }.toSeq)
        case None => <.div("Loading properties")
      }
    }

    def on_prop_update(props: Props)(action: RuntimeAction): F[Unit] = {
      action match {
        case SetValue(_, _) => load_props(props).runNow()
        case _ =>
      }
      mt.pure()
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
