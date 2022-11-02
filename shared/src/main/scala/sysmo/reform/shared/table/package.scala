package sysmo.reform.shared

import cats.Show

package object table {
  object implicits {
    implicit val show_table: Show[LocalTable] = new Show[LocalTable] {
      override def show(t: LocalTable): String = new TablePrinter(t).print
    }
  }
}
