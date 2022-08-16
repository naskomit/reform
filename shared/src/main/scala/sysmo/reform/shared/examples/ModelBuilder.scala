package sysmo.reform.shared.examples

import sysmo.reform.shared.types
import sysmo.reform.shared.types.{RecordFieldType, RecordType, UnionType}

trait ModelBuilder extends
  RecordFieldType.Constr with UnionType.Constr {
  def record(symbol: String): RecordType.Builder =
    new RecordType.Builder(symbol)

}
