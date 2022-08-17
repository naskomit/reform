package sysmo.reform.shared.util

class SequenceIndex[K, V](s: Seq[V], f: V => K) {
  protected val lookup: Map[K, Int] = s.zipWithIndex.map(x => (f(x._1), x._2)).toMap
  def get(key: K): Option[V] = lookup.get(key).map(i => s(i))
  def get_index(key: K): Option[Int] = lookup.get(key)
}
