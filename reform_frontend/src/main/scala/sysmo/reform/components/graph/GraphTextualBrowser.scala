package sysmo.reform.components.graph

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.gremlin.{tplight => TP}

object GraphTextualBrowser extends ReactComponent {

  case class Props(graph: TP.Graph)
  case class State(current_vertex: Option[TP.Vertex])

  final class Backend($: BackendScope[Props, State]) {
    def elem_str(elem: TP.Element): String = {
      s"${elem.id}: ${elem.label}"
    }
    def prop_str(elem: TP.Element): String = {
      elem.properties
        .filter((p: TP.Property[_]) => p.value.isDefined)
        .map((p: TP.Property[_]) => s"${p.key}: ${p.value.get}")
        .mkString(", ")
    }
    def render (p: Props, s: State): VdomElement = {
      <.div(^.className:= "form",
        <.h1("Graph browser"),
        p.graph.vertices().map(v => {
          <.div(^.key:= v.id.toString,
            <.div(<.h3(s"V[${elem_str(v)}]"), <.h6(s"{${prop_str(v)}}")),
            v.edges(TP.Direction.OUT, Seq()).map(e => {
              val in_v = e.in_vertex
              <.div(^.key:= e.id.toString,
                <.div(^.marginLeft:= 10.px ,s"->E[${elem_str(e)}]{${prop_str(e)}}"),
                <.div(^.marginLeft:= 20.px ,s"->V[${elem_str(in_v)}]{${prop_str(in_v)}}"),
              )
            }).toTagMod,
            v.edges(TP.Direction.IN, Seq()).map(e => {
              val out_v = e.out_vertex
              <.div(^.key:= e.id.toString,
                <.div(^.marginLeft:= 10.px ,s"<-E[${elem_str(e)}]{${prop_str(e)}}"),
                <.div(^.marginLeft:= 20.px ,s"<-V[${elem_str(out_v)}]{${prop_str(out_v)}}"),
              )
            }).toTagMod
          )
        }).toTagMod
      )
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ReactComponentTemplate")
    .initialState(State(None))
    .renderBackend[Backend]
    .build

  //
  def apply(graph: TP.Graph): Unmounted = {
    component(Props(graph))
  }
}
