package sysmo.reform.shared.gremlin.bytecode

object Order extends Enumeration {
  type Order = Value
  val incr, decr, shuffle, asc, desc = Value

  def has(v: Any): Boolean = v match {
    case v : Order if values.contains(v) => true
    case _ => false
  }}
