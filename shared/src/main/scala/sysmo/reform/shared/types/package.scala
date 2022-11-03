package sysmo.reform.shared

import sysmo.reform.shared.expr.Expression

package object types {
  trait HasName {
    def name: String
    def descr: Option[String]
    def make_descr: String = descr.getOrElse(name)
  }

  trait HasSymbol {
    val symbol: String
    val descr: Option[String]
    def make_descr: String = descr.getOrElse(symbol)
  }

  trait HasLabelExpr {
    val label_expr: Option[Expression]
  }

}
