package sysmo.reform.shared.types
import sysmo.reform.shared.data.ObjectId

trait UnionTypeAux extends DataTypeAux[UnionType] {
  class Builder(val obj: UnionType) extends DataTypeBuilder[UnionType] {

  }

  implicit def builder2type(builder: Builder): UnionType = builder.build

  def apply(symbol: String, subtypes: RecordType.Builder*): Builder =
    new Builder(new UnionType(symbol, None, subtypes.map(x => x: RecordType)))

}
