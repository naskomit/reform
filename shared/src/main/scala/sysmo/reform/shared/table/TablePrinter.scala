package sysmo.reform.shared.table

import cats.Show

import java.io.StringWriter
import scala.collection.mutable




class TablePrinter(t: LocalTable) {
  val ncol: Int = t.schema.fields.size
  lazy val col_names: Seq[String] = t.schema.fields.map(ftype => ftype.name)

  lazy val col_widths: Seq[Int] = {
    val ncol = t.schema.fields.size
    val widths  = mutable.ArraySeq.from(col_names.map(_.length))
    for (row <- t) {
      for (icol <- 0 until ncol) {
        val width = row.get(icol).show().length
        widths(icol) = Math.max(widths(icol), width)
      }
    }
    widths.toSeq
  }

  lazy val row_separator: String = (col_widths.map(width => "-" * width)).mkString("+-", "-+-", "-+")

  private def make_header_row: String = {
    col_names.zip(col_widths).map {
      case (name, width) => ("%" + width + "s").format(name)
    }.mkString("| ", " | ", " |")
  }

  private def make_content_row(row: Table.Row): String = {
    col_widths.zipWithIndex.map {
      case (width, index) => ("%" + width + "s").format(row.get(index).show().trim)
    }.mkString("| ", " | ", " |")
  }

  object appender {
    var content = ""
    def +(row: String): this.type = {
      content = content + row + "\n"
      this
    }
  }

  def print: String = {
    appender + row_separator + make_header_row + row_separator
    for (row <- t) {
      appender + make_content_row(row)
    }
    appender + row_separator
    appender.content
  }
}
