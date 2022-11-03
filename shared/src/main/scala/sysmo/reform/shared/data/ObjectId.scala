package sysmo.reform.shared.data

import java.util.UUID

trait ObjectId {
  type Id
  val v: Id
  override def hashCode(): Int = v.hashCode()
  override def equals(other: Any): Boolean = other match {
    case o: ObjectId => v == o.v
    case _ => false
  }
  def serialize: String = v.toString
  def show: String
}

object ObjectId {
  object NoId extends ObjectId {
    override type Id = Unit
    val v = ()
    def show: String = "<N/A>"
  }
}

trait ObjectIdSupplier {
  def new_id: ObjectId
}

case class StringObjectId(v: String) extends ObjectId {
  type Id = String
  def show: String = v
}

case class UUObjectId(v: UUID) extends ObjectId {
  type Id = UUID
  def show: String = v.toString
}

class UUIDSupplier() extends ObjectIdSupplier {
  val start = UUObjectId(UUID.randomUUID())
  var last_uuid = start
  override def new_id: ObjectId = {
    val id = UUObjectId(UUID.randomUUID())
    last_uuid = id
    id
  }
}


case class NumbericObjectId(v: Long) extends ObjectId {
  type Id = Long
  def show: String = v.toString
}
