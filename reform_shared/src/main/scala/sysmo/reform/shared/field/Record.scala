package sysmo.reform.shared.field

import io.circe.{Decoder, JsonObject}

//trait Record {
//  val schema: RecordType[_]
//  protected def _get(col: Int): Value[_]
//  def get(col: Int): Value[_] =
//    if ((col >= 0) && (col < n_fields)) _get(col) else Value.empty()
//  def get(col: String): Value[_] = schema.field_index(col) match {
//    case Some(ind) => _get(ind)
//    case None => Value.empty()
//  }
//  def names: Seq[String] = schema.fields.map(_.name)
//  def n_fields: Int = schema.fields.length
//
//}

trait Schema {
  def symbol: String
  def fields: Seq[Field]

  def names: Seq[String] = fields.map(_.name)
  def n_fields: Int = fields.length

  def field(index: Int): Option[Field]
  def field_index(name: String): Option[Int]

  def field(name: String): Option[Field] = field_index(name).map(index => fields(index))
}

trait Record {
  def schema: Schema
  protected def _get(col: Int): Value[_]
  def get(col: Int): Value[_] =
    if ((col >= 0) && (col < schema.n_fields)) _get(col) else Value.empty()
  def get(col: String): Value[_] = schema.field_index(col) match {
    case Some(ind) => _get(ind)
    case None => Value.empty()
  }


  //  def encode(v: A): JsonObject
  //  def decode(x: JsonObject): Decoder.Result[A]
}

trait RecordRegistry {
  def record_type(name: String): Option[Record]
}
