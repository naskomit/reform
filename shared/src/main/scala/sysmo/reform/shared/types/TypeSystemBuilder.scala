package sysmo.reform.shared.types

trait TypeSystemBuilder extends RecordFieldType.Constr {
  var type_map: Map[String, DataType] = Map()

  def record(symbol: String): RecordType.Builder = {
    val new_type = RecordType(symbol)
    type_map = type_map + (symbol -> new_type)
    new_type
  }

  def union(symbol: String, subtypes: RecordType.Builder*): UnionType.Builder = {
    val new_type = UnionType(symbol, subtypes: _*)
    type_map = type_map + (symbol -> new_type)
    new_type
  }

  def build: TypeSystem = TypeSystem(type_map)
}

case class TypeSystem(type_map: Map[String, DataType]) {
  def get(symbol: String): Option[DataType] = type_map.get(symbol)
}