package sysmo.reform.shared.runtime

import sysmo.reform.shared.data.{ObjectId, Value}

trait RuntimeAction

case class SetValue(id: ObjectId, value: Value) extends RuntimeAction
