package sysmo.reform.shared.types
import sysmo.reform.shared.data.ObjectId

trait ReferenceTypeAux {
  class Builder(protected[ReferenceTypeAux] val _prototype: CompoundDataType) extends DataTypeBuilder[ReferenceType] {

  }

  implicit class Impl(builder: Builder) extends ReferenceType {
    override def id: ObjectId = builder._id
    override def prototype: CompoundDataType = builder._prototype
    override def show: String = s"Ref[${builder._prototype.symbol}]"
  }
}

trait MultiReferenceTypeAux {
  class Builder(protected[MultiReferenceTypeAux] val _prototype: CompoundDataType) extends DataTypeBuilder[ReferenceType] {

  }

  implicit class Impl(builder: Builder) extends MultiReferenceType {
    override def id: ObjectId = builder._id
    override def prototype: CompoundDataType = builder._prototype
    override def show: String = s"MultiRef[${builder._prototype.symbol}]"
  }

}
