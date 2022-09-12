package sysmo.reform.react.property

import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.sources.property.{LocalPropertySource, PropertySource, PropertyTypes}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.layout.{FlexFormLayout, FormItem, FormLayout}
import sysmo.reform.effects.implicits._

case class PropertySourceView()
case class PropertyView()

class PropertyGroupEditorF[PT <: PropertyTypes, F[+_]](implicit f2c: F2Callback[F]) extends ReactComponent {
  case class Props(ps: PropertySource[PT, F], layout: FormLayout)
  case class State(ps_local: Option[LocalPropertySource[PT, F]])
  class Backend($: BScope) {
    def render(props: Props, state: State): VdomElement = {

      state.ps_local match {
        case Some(ps) => props.layout(ps.props_sync.map(x =>
          FormItem(x.descr, <.div(x.name))
        ).toSeq)
        case None => <.div("Loading properties")
      }
    }

    def load_props(props: Props): AsyncCallback[Unit] = {
      for {
        p_seq <- f2c.async(props.ps.cache)
        _ <- $.modStateAsync(s => s.copy(ps_local = Some(p_seq)))
      } yield {
        println("Props loaded")
        ()
      }
    }
  }

  val component = ScalaComponent.builder[Props]("ColumnsLayoutComponent")
      .initialState(State(None))
      .renderBackend[Backend]
      .componentDidMount(f => f.backend.load_props(f.props))
      .build

  def apply(ps: PropertySource[PT, F]): Unmounted =
    component(Props(ps, FlexFormLayout))
}
