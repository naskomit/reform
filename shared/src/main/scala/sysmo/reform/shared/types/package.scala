package sysmo.reform.shared

import sysmo.reform.shared.expr.Expression

package object types {
  trait HasName {
    def name: String
    def descr: Option[String]
    def make_descr: String = descr.getOrElse(name)
  }

  trait HasSymbol {
    def symbol: String
    def descr: Option[String]
    def make_descr: String = descr.getOrElse(symbol)
  }

  trait HasLabelExpr {
    def label_expr: Option[Expression]
  }


  trait HasSymbolBuilder {
    val symbol: String
    protected[types] var _descr: Option[String] = None
    def descr(v: String): this.type = {
      _descr = Some(v)
      this
    }
  }

  object instances {
    val x = RecordType
  }
}
