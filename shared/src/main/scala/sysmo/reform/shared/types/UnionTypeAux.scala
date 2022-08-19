package sysmo.reform.shared.types
import sysmo.reform.shared.data.ObjectId

trait UnionTypeAux {
  class Builder(val symbol: String, val subtypes: Seq[RecordType.Builder]) extends DataTypeBuilder[UnionType]
  with HasSymbolBuilder {
  }

  implicit class UnionTypeImpl(builder: Builder) extends UnionType {
    override def id: ObjectId = builder._id
    override def symbol: String = builder.symbol
    override def descr: Option[String] = builder._descr
    override def subtypes: Seq[RecordType] = builder.subtypes.map(x => x: RecordType)
    override def supertype_of(r: RecordType): Boolean = subtypes.contains(r)
    override def show: String = s"Union[${builder.symbol}]"
  }

//  trait Constr {
//    def union(symbol: String, subtypes: RecordType.Builder*): Builder =
//      new Builder(symbol, subtypes)
//  }

  def apply(symbol: String, subtypes: RecordType.Builder*): Builder =
    new Builder(symbol, subtypes)

}
