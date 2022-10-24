package sysmo.reform.shared.runtime

import sysmo.reform.shared.data.{ObjectId, Value}

trait RuntimeAction

case class SetFieldValue(id: ObjectId, new_value: RecordFieldInstance) extends RuntimeAction
