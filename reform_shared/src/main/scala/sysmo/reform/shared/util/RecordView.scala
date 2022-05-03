package sysmo.reform.shared.util

trait RecordView[A] {
  def get(col: Int): A
  def get(col: String): A
  def names: Seq[String]
  def n_fields: Int
  def to_map: Map[String, A] =
    names.map { x => (x, get(x))}.toMap
}
