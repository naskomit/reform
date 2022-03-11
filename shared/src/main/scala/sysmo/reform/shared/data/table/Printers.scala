package sysmo.reform.shared.data.table

import sysmo.reform.shared.util.pprint.PrettyPrinter

object Printers {
  implicit val vector_printer = new PrettyPrinter[Series] {
    override def pprint(v: Series): String = {
      val sb = new StringBuilder("")
      sb ++= v.field.field_type.tpe.toString
      sb ++= "("
      v.foreach {
        case x if x.is_set => sb++= x.as_char.get + ", "
        case _ => sb++= "N/A, "
      }
      sb.deleteCharAt(sb.length - 1)
      sb.deleteCharAt(sb.length - 1)
      sb ++= ")"
      sb.toString
    }
  }

  implicit val table_printer = new PrettyPrinter[Table] {
    override def pprint(v: Table): String = {
      val max_field_width = 10
      val v_delim = "="
      val h_delim = "|"
      val sb = new StringBuilder("")
      val padding = 3
      val field_width = v.schema.fields.map(f => f.name.length + 2 * padding)
      val vdelim = v_delim * (field_width.sum + v.schema.fields.length + 1)
      // top line
      sb ++= vdelim
      sb ++= "\n"
      // col names
      sb ++= h_delim
      v.schema.fields.foreach(f => {
        sb ++= " " * padding
        sb ++= f.name
        sb ++= " " * padding
        sb ++= h_delim
      })
      sb ++= "\n"
      // header line
      sb ++= vdelim
      sb ++= "\n"
      for (row <- v.row_iter) {
        sb ++= h_delim
        for (col <- 0 until v.schema.fields.size) {
          val value_rep = row.get(col).as_char match {
            case Some(x) => x
            case None => "N/A"
          }
          val extra_padding_size = field_width(col) - value_rep.length - padding
          sb ++= " " * padding
          sb ++= value_rep + (" " * extra_padding_size)
          sb ++= h_delim
        }
        sb ++= "\n"
        sb ++= vdelim
        sb ++= "\n"
      }
      sb.toString
    }
  }
}
