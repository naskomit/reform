package sysmo.reform.shared.property

trait PropertySource {
  type K
  def properties: Seq[Property[K, _]]
}
