package sysmo.demo1

import cats.{MonadError, MonadThrow}
import japgolly.scalajs.react.extra.internal.StateSnapshot
import japgolly.scalajs.react.extra.internal.StateSnapshot.withReuse
import japgolly.scalajs.react.util.{Effect, Semigroup}
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.app.{Configuration, Panel}
import sysmo.reform.explorers.RuntimeExplorerF
import sysmo.reform.shared.examples.MicroController
import sysmo.reform.shared.runtime.{FLocal, ObjectRuntime, RuntimeObject}

import scala.scalajs.js
import scala.scalajs.js.{Promise, Thenable}

object RuntimeExplorerPanel extends Panel {

  import japgolly.scalajs.react._
  type F[+X] = FLocal[X]
  val mt: MonadThrow[F] = MonadThrow[F]
  object RuntimeExplorer extends RuntimeExplorerF[F]
  case class Props(initial_obj: F[RuntimeObject[F]])
  case class State(current_object: Option[RuntimeObject[F]], runtime: Option[ObjectRuntime[F]])

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      <.div(
        <.div(^.cls:= "page-title",
          <.h1("Runtime Explorer")
        ),
        <.div(^.cls:= "wrapper wrapper-white",
          s.runtime match {
            case None => <.div("Loading")
            case Some(rt) => RuntimeExplorer(rt)
          }
        )
      )
    }
  }

//  implicit val ed: Effect.Dispatch[F] = new Effect.Dispatch[F] {
//    override def dispatch[A](fa: F[A]): Unit = {}
//    override def dispatchFn[A](fa: => F[A]): js.Function0[Unit] = () => fa
//    override def delay[A](a: => A): F[A] = mt.pure(a)
//    override def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA] =
//      mt.handleErrorWith[AA](fa)(f)
//    override def suspend[A](fa: => F[A]): F[A] = fa
//    override def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A] = {
//      val result = mt.map(fa)(x => {runFinally; x})
//      mt.handleErrorWith(result)(e => {runFinally; result})
//    }
//
//    override def pure[A](a: A): F[A] = mt.pure(a)
//    override def map[A, B](fa: F[A])(f: A => B): F[B] = mt.map(fa)(f)
//    override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = mt.flatMap(fa)(f)
//    override def tailrec[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = mt.tailRecM(a)(f)
//  }
//
//  implicit class des(f: F[_]) extends Effect.Sync[F] {
//    override val empty: F[Unit] = _
//    override val semigroupSyncOr: Semigroup[F[Boolean]] = _
//    override implicit val semigroupSyncUnit: Semigroup[F[Unit]] = _
//
//    override def fromJsFn0[A](f: js.Function0[A]): F[A] = ???
//    override def isEmpty[A](f: F[A]): Boolean = ???
//    override def reset[A](fa: F[A]): F[Unit] = ???
//    override def runAll[A](callbacks: F[A]*): F[Unit] = ???
//    override def runSync[A](fa: => F[A]): A = ???
//    override def sequence_[A](fas: => Iterable[F[A]]): F[Unit] = ???
//    override def sequenceList[A](fas: => List[F[A]]): F[List[A]] = ???
//    override def toJsFn[A](fa: => F[A]): js.Function0[A] = ???
//    override def traverse_[A, B](as: => Iterable[A])(f: A => F[B]): F[Unit] = ???
//    override def traverseList[A, B](as: => List[A])(f: A => F[B]): F[List[B]] = ???
//    override def when_[A](cond: => Boolean)(fa: => F[A]): F[Unit] = ???
//    override def dispatch[A](fa: F[A]): Unit = {}
//    override def dispatchFn[A](fa: => F[A]): js.Function0[Unit] = () => fa
//    override def delay[A](a: => A): F[A] = mt.pure(a)
//    override def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA] =
//      mt.handleErrorWith[AA](fa)(f)
//    override def suspend[A](fa: => F[A]): F[A] = fa
//    override def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A] = {
//      val result = mt.map(fa)(x => {runFinally; x})
//      mt.handleErrorWith(result)(e => {runFinally; result})
//    }
//
//    override def pure[A](a: A): F[A] = mt.pure(a)
//    override def map[A, B](fa: F[A])(f: A => B): F[B] = mt.map(fa)(f)
//    override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = mt.flatMap(fa)(f)
//    override def tailrec[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = mt.tailRecM(a)(f)
//  }
//  import scala.scalajs.js.Thenable.Implicits._
  val component = ScalaComponent.builder[Props]("RuntimeExplorer")
    .initialState(State(None, None))
    .renderBackend[Backend]
//    .componentDidMount{f =>
//      mt.map(f.props.initial_obj)(obj =>
//        f.modState((s: State) =>
//          s.copy(current_object = Some(obj), runtime = Some(obj.get_runtime))
//        )
//      )
//    }
    .build

  def apply(app_config: Configuration): Unmounted = {
    val controller = MicroController.initializer1()
    component(Props(controller))
  }
}