package sysmo.reform.shared.gremlin

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

}

case class TextPredicate(biPredicate: TextP.Value, value: String)

case object TextPredicate {
  def startingWith(value: String): TextPredicate = TextPredicate(TextP.startingWith, value)
  def notStartingWith(value: String): TextPredicate = TextPredicate(TextP.notStartingWith, value)
  def endingWith(value: String): TextPredicate = TextPredicate(TextP.endingWith, value)
  def notEndingWith(value: String): TextPredicate = TextPredicate(TextP.notEndingWith, value)
  def containing(value: String): TextPredicate = TextPredicate(TextP.containing, value)
  def notContaining(value: String): TextPredicate = TextPredicate(TextP.notContaining, value)
}