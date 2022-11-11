package sysmo.reform.shared.types

import cats.MonadThrow

trait TypeSystemBuilder extends RecordFieldType.Constr {
  var type_map: Map[String, DataTypeBuilder[DataType]] = Map()

  def record(symbol: String): RecordType.Builder = {
    val new_type = RecordType(symbol)
    type_map = type_map + (symbol -> new_type.asInstanceOf[DataTypeBuilder[DataType]])
    new_type
  }

  def union(symbol: String, subtypes: RecordType.Builder*): UnionType.Builder = {
    val new_type = UnionType(symbol, subtypes: _*)
    type_map = type_map + (symbol -> new_type.asInstanceOf[DataTypeBuilder[DataType]])
    new_type
  }

  def build: TypeSystem = TypeSystem(type_map.map {
    case (symbol, builder) => (symbol, builder.build)
  })
}

case class TypeSystem(type_map: Map[String, DataType]) {
  def records: Iterable[RecordType] = type_map.values.collect {
    case x: RecordType => x
  }
  def arrays: Iterable[ArrayType] = type_map.values.collect {
    case x: ArrayType => x
  }
  def get(symbol: String): Option[DataType] = type_map.get(symbol)

  def get_record_type[F[+_]](symbol: String)(implicit mt: MonadThrow[F]) = get(symbol) match {
    case Some(rec: RecordType) => mt.pure(rec)
    case Some(x) => mt.raiseError(new IllegalArgumentException(s"Expected ${symbol} to be a record type, found ${x}"))
    case None => mt.raiseError(new IllegalArgumentException(s"Cannot find type ${symbol} in the type system"))
  }
}