package sysmo.reform.components.forms

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ReactComponent
import sysmo.reform.shared.form.{runtime => FR}
import japgolly.scalajs.react._
import sysmo.reform.shared.form.runtime.{AllValues, MultiValue, NoValue, SomeValue}

object RuntimeTextualBrowser extends ReactComponent {

  case class Props(runtime: FR.FormRuntime)
  case class State()

  /** Viewer for atomic objects */
  object ValueBrowser {
    case class Props(value: FR.AtomicValue)
    def render(p: Props): VdomElement = {
      p.value.value match {
        case NoValue => <.span("_")
        case AllValues => <.span("ALL")
        case SomeValue(x) => <.span(x.make_label)
        case MultiValue(x) => <.span("[", x.map(_.make_label).mkString(", "), "]")
      }
    }

    val component = ScalaComponent.builder[Props]("ValueBrowser")
      .render_P(render)
      .build

    def apply(value: FR.AtomicValue) = component(Props(value))
  }

  /** Viewer for ref objects */
  object RefViewer {
    case class Props(ref: FR.ObjectId, runtime: FR.FormRuntime)
    case class State(expanded: Boolean)
    final class Backend($: BackendScope[Props, State]) {
      def render(p: Props, s: State): VdomElement = {
        val referenced = p.runtime.objects(p.ref)
        referenced match {
          case v: FR.AtomicValue => ValueBrowser(v)
          case _ => <.span(
            ^.onClick ==> toggle,
            if (s.expanded) {
              referenced match {
                case v: FR.Group => GroupBrowser(v, p.runtime)
                case v: FR.Array => ArrayBrowser(v, p.runtime)
              }
            } else {
              <.span("#", p.ref.id)
            }
          )
        }

      }

      def toggle(e: ReactEvent): Callback = Callback {
        e.stopPropagation()
      } >> $.modState(s => s.copy(expanded = !s.expanded))

    }
    val component = ScalaComponent.builder[Props]("ValueBrowser")
      .initialState(State(false))
      .renderBackend[Backend]
      .build

    def apply(ref: FR.ObjectId, runtime: FR.FormRuntime) = component(Props(ref, runtime))
  }

  /** Viewer for arrays */
  object ArrayBrowser {
    case class Props(array: FR.Array, runtime: FR.FormRuntime)
    def render(p: Props): VdomElement = {
      <.div(^.id:= s"rtb_${p.array.id.id}",
        <.h3(p.array.id.id, ": Array[", p.array.prototype.prototype.symbol, "]"),
        p.array.children.map(c =>
          <.div(^.marginLeft:=20.px , "- ", RefViewer(c, p.runtime))
        ).toTagMod)
    }

    val component = ScalaComponent.builder[Props]("ArrayBrowser")
      .render_P(render)
      .build

    def apply(array: FR.Array, runtime: FR.FormRuntime) = component(Props(array, runtime))
  }

  /** Viewer for arrays */
  object GroupBrowser {
    case class Props(group: FR.Group, runtime: FR.FormRuntime)
    def render(p: Props): VdomElement = {
      <.div(
        <.h3(^.id:= s"rtb_${p.group.id.id}", p.group.id.id, ": Group[", p.group.prototype.symbol, "]"),
        p.group.children.map(c =>
          <.div(^.marginLeft:=20.px , "- ", c._1, ": ", RefViewer(c._2, p.runtime))
        ).toTagMod)
    }

    val component = ScalaComponent.builder[Props]("GroupBrowser")
      .render_P(render)
      .build

    def apply(group: FR.Group, runtime: FR.FormRuntime) = component(Props(group, runtime))
  }


  final class Backend($: BackendScope[Props, State]) {
    def render_objects(runtime: FR.FormRuntime): VdomElement = {
      <.div(
        runtime.objects.toMap.collect {
          case (_, g: FR.Group) => GroupBrowser(g, runtime)
          case (_, a: FR.Array) => ArrayBrowser(a, runtime)
        }
        .toTagMod
      )

    }
    def render (p: Props, s: State): VdomElement = {
      <.div(
        <.h1("Runtime browser"),
        render_objects(p.runtime)
      )
    }
  }

  // : Scala.Component[Props, State, Backend, _]
  val component =
    ScalaComponent.builder[Props]("ReactComponentTemplate")
    .initialState(State())
    .renderBackend[Backend]
    .build

  //
  def apply(runtime: FR.FormRuntime): Unmounted = {
    component(Props(runtime))
  }
}