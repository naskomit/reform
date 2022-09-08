package sysmo.reform.shared.sources.property

import sysmo.reform.shared.data.Value
import sysmo.reform.shared.types.DataType

trait Property {
  type Id
  def id: Id
  def name: String
  def descr: String
  def dtype: DataType
  def value: Value
}
