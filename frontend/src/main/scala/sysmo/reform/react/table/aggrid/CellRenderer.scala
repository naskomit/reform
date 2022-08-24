package sysmo.reform.react.table.aggrid

import org.scalajs.dom
import sysmo.reform.shared.data.{ObjectId, Value}
import Value.implicits._

import java.util.Date

trait CellRenderer {
  val AGF = AgGridFacades
  def render(x: AGF.ICellRendererParams): dom.HTMLElement
}

object IdCellRenderer extends CellRenderer {
  override def render(x: AGF.ICellRendererParams): dom.HTMLElement = {
    val v = x.value.asInstanceOf[Value]
    val id = v.get[ObjectId].map(_.show)
    val child = dom.document.createElement("span")

    child.textContent = id.getOrElse("<N/A>")
    child.asInstanceOf[dom.HTMLElement]
  }
}

object TextCellRenderer extends CellRenderer {
  override def render(x: AGF.ICellRendererParams): dom.HTMLElement = {
    val v = x.value.asInstanceOf[Value]
    val child = dom.document.createElement("span")
    child.textContent = v.get[String].getOrElse("<N/A>")
    child.asInstanceOf[dom.HTMLElement]
  }
}

object DateCellRenderer extends CellRenderer {
  override def render(x: AGF.ICellRendererParams): dom.HTMLElement = {
    val v = x.value.asInstanceOf[Value]
    val child = dom.document.createElement("span")
    child.textContent = v.get[Date].map(date =>
      (new scala.scalajs.js.Date(date.getTime)).toDateString()
    ).getOrElse("<N/A>")
    child.asInstanceOf[dom.HTMLElement]
  }
}