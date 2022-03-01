package sysmo.reform.shared.gremlin.bytecode

trait BiPredicate extends Enumeration

object Compare extends BiPredicate {
  type Compare = Value
  val eq, neq, gt, gte, lt, lte = Value
}

object Contains extends BiPredicate {
  type Contains = Value
  val within, without = Value
}

object TextP extends BiPredicate {
  type Text = Value
  val startingWith, notStartingWith,endingWith, notEndingWith, containing, notContaining = Value
  def has(v: BiPredicate#Value): Boolean = v match {
    case v : Text if values.contains(v) => true
    case _ => false
  }
}



case class Predicate[V](biPredicate: BiPredicate#Value, value: V)

object Predicate {
  def eq_[V](value: V): Predicate[V] = Predicate(Compare.eq, value)
  def neq[V](value: V): Predicate[V] = Predicate(Compare.neq, value)
  def gt[V](value: V): Predicate[V] = Predicate(Compare.gt, value)
  def gte[V](value: V): Predicate[V] = Predicate(Compare.gte, value)
  def lt[V](value: V): Predicate[V] = Predicate(Compare.lt, value)
  def lte[V](value: V): Predicate[V] = Predicate(Compare.lte, value)

  def within[V](value: Iterable[V]) = Predicate(Contains.within, value)
  def without[V](value: Iterable[V]) = Predicate(Contains.without, value)

  def startingWith(value: String): Predicate[String] = Predicate(TextP.startingWith, value)
  def notStartingWith(value: String): Predicate[String] = Predicate(TextP.notStartingWith, value)
  def endingWith(value: String): Predicate[String] = Predicate(TextP.endingWith, value)
  def notEndingWith(value: String): Predicate[String] = Predicate(TextP.notEndingWith, value)
  def containing(value: String): Predicate[String] = Predicate(TextP.containing, value)
  def notContaining(value: String): Predicate[String] = Predicate(TextP.notContaining, value)
}