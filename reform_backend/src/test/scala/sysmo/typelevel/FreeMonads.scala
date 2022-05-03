package sysmo.typelevel

object FreeMonads extends App {
  sealed trait KVStoreA[A]
  case class Put[T](key: String, value: T) extends KVStoreA[Unit]
  case class Get[T](key: String)  extends KVStoreA[Option[T]]
  case class Delete(key: String) extends KVStoreA[Unit]

//  import cats.free.Free
//
//  type KVStore[A] = Free[KVStoreA, A]
}
