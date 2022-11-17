package sysmo.reform.shared.util

class SequenceIndex[K, V](s: IndexedSeq[V], f: V => K) {
  protected val lookup: Map[K, Int] = s.zipWithIndex.map(x => (f(x._1), x._2)).toMap
  def get(index: Int): Option[V] = try {
    Some(s.apply(index))
  } catch {
    case e: IndexOutOfBoundsException => None
  }
  def get(key: K): Option[V] = lookup.get(key).map(i => s(i))
  def get_index(key: K): Option[Int] = lookup.get(key)
  def toSeq: Seq[V] = s
  def toMap: Map[K, V] = lookup.map {
    case (k, i) => (k, s(i))
  }
}

object SequenceIndex {
  /** Creates a data structure that could be accessed by numerical
   * indices or by keys */
  def apply[K, V](s: Seq[V], f: V => K): SequenceIndex[K, V] =
    new SequenceIndex[K, V](s.toIndexedSeq, f)

}
