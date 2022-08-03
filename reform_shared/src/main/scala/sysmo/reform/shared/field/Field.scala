package sysmo.reform.shared.field

trait Field {
  def name: String
  def descr: Option[String]
  def make_descr: String = descr.getOrElse(name)
  def ftype: FieldType
  def nullable: Boolean
}
