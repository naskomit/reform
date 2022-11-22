package sysmo.reform.shared.actions

import cats.MonadThrow
import cats.syntax.all._
import sysmo.reform.shared.data.{ObjectId, Value}
import Value.implicits._

trait Action

trait SideEffect {
  def group: ObjectId
  def +(other: SideEffect): SequentialEffects =
    SequentialEffects(Seq(this, other))
  def to_program: SequentialEffects =
    SequentialEffects(Seq(this))
}

case class SequentialEffects(actions: Seq[SideEffect]) extends Action {
  def and_then(other: SideEffect): SequentialEffects =
    SequentialEffects(actions :+ other)
  def +(other: SideEffect): SequentialEffects =
    and_then(other)
}

object Action {
  def apply(action: SideEffect): Action = {
    SequentialEffects(Seq(action))
  }
}

