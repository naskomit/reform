package sysmo.demo1

import cats.MonadThrow
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Panel}

object HomePanel extends Panel {

  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls:= "page-title",
          <.h1("Reform Demo 1")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          "Description"
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
//    .componentDidMount{f =>
//      f.props.mt.map(f.props.initialization){ rt =>
//        f.modState(s => s.copy(runtime = ))
//
//      })
//    }
    .build


  def apply(): Unmounted = {
    component(Props())
  }
//    val controllerF = MicroController.initializer1()
//
//    component(new Props {
//      type F[+X] = MicroController.initializer1.F[X]
//      val mt = MicroController.initializer1.runtime.mt
//      val initialization = controllerF
//    })
//  }
}