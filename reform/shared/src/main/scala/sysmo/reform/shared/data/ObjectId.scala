package sysmo.reform.shared.data

import java.util.UUID

trait ObjectId {
  type Id
  val v: Id
  def next: ObjectId
}

object NoId extends ObjectId {
  override type Id = Unit
  val v = ()
  override def next: ObjectId = this
}

trait ObjectIdSupplier {
  def new_id: ObjectId
}

case class UUObjectId(v: UUID) extends ObjectId {
  type Id = UUID
  def next: UUObjectId = UUObjectId(UUID.randomUUID())
  override def hashCode(): Int = v.hashCode()
  override def equals(other: Any): Boolean = other match {
    case UUObjectId(v_other) => v == v_other
    case _ => false
  }
}

class UUIDSupplier() extends ObjectIdSupplier {
  val start = UUObjectId(UUID.randomUUID())
  var last_uuid = start
  override def new_id: ObjectId = {
    val id = last_uuid.next
    last_uuid = id
    id
  }
}