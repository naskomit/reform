package sysmo.reform.react.table

import org.scalajs.dom
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.data.Value.implicits._

import java.util.Date

trait CellRenderer {
  def render(v: Value): dom.HTMLElement = {
    render_formatted(
      format(v)
    )
  }
  def format(v: Value): Option[String]
  def render_na: String = "<N/A>"
  def render_formatted(x: Option[String]): dom.HTMLElement = {
    val child = dom.document.createElement("span")
    child.textContent = x.getOrElse(render_na)
    child.asInstanceOf[dom.HTMLElement]
  }
}

object IdCellRenderer extends CellRenderer {
  override def format(v: Value): Option[String] =
    v.get[ObjectId].map(_.show)
}

object TextCellRenderer extends CellRenderer {
  override def format(v: Value): Option[String] =
    v.get[String]
}

object DateCellRenderer extends CellRenderer {
  override def format(v: Value): Option[String] =
    v.get[Date].map(date =>
      (new scala.scalajs.js.Date(date.getTime)).toDateString()
    )
}