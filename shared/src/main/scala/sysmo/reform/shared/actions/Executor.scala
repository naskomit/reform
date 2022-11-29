package sysmo.reform.shared.actions

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.ObjectId

trait Executor[F[+_]] {
  implicit val mt: MonadThrow[F]
  def execute(program: Action): F[Unit]
}

trait EffectHandler[F[+_]] {
  type T <: SideEffect
  implicit val mt: MonadThrow[F]
  def handle_effect(action: T): F[Unit]
}

class ActionExecutor[F[+_]](action_groups: Map[ObjectId, EffectHandler[F]])(implicit val mt: MonadThrow[F])
  extends Executor[F] {

  override def execute(program: Action): F[Unit] = {
    program match {
      case SequentialEffects(actions) => actions
        .traverse { action =>
          action_groups.get(action.group) match {
            // TODO Here perhaps should check the type of handler???
            case Some(handler) =>
              handler.handle_effect(action.asInstanceOf[handler.T])
            case None =>
              mt.raiseError(new IllegalArgumentException(s"Unknown action group ${action.group}"))
          }
        }.map(x => ())
      case Action.None => mt.pure(())
      case _ => mt.raiseError(new IllegalArgumentException(
        s"Cannot execute action program ${program}")
      )
    }
  }
}

object ActionExecutor {
  def apply[F[+_]](action_groups: Map[ObjectId, EffectHandler[F]])
                  (implicit mt: MonadThrow[F]): ActionExecutor[F] =
    new ActionExecutor[F](action_groups)
}