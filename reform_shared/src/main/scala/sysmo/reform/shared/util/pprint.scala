package sysmo.reform.shared.util

object pprint {
  // Typeclass for pretty printing
  trait PrettyPrinter[V] {
    def pprint(v: V): String
  }

  def pr_str[V : PrettyPrinter](v: V)(implicit pp: PrettyPrinter[V]): String = {
    pp.pprint(v)
  }

  def pprint[V : PrettyPrinter](v: V)(implicit pp: PrettyPrinter[V]): Unit = {
    println(pp.pprint(v))
  }


}
