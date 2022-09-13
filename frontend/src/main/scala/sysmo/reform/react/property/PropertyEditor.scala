package sysmo.reform.react.property

import sysmo.reform.effects.implicits.F2Callback
import sysmo.reform.react.ReactComponent
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.{sources => S}

trait PropertyEditor[F[+_]] extends ReactComponent {
  implicit val f2c: F2Callback[F]
  trait PropsBase {
    val id: ObjectId
    val value: Value
    val dispatcher: Dispatcher[F]
  }
  type Props <: PropsBase
  type Dispatcher[F[+_]]= S.Dispatcher[F]

}

object PropertyEditor {
  sealed trait Status
  case object Valid extends Status
  case class Error(msg: String, value: String) extends Status
  case class Warning(msg: String, value: String) extends Status



}