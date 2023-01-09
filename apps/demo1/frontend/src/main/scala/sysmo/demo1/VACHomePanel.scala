package sysmo.demo1

import cats.MonadThrow
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Panel}

object VACHomePanel extends Panel {

  import japgolly.scalajs.react._

  case class Props()
  case class State()

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      val apps = Seq(
        ("Virtual Anatomy Collection Metadata", "metadata/")
      )


      <.div(
        <.div(^.cls:= "page-title",
          <.h1("Virtual Anatomy Collection Applications")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          <.h3("Existing applications"),
          <.ul(
            apps.map {
              case (name, url) => <.li(<.a(
                ^.href:= url, name
              ))
            }.toTagMod
          )
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