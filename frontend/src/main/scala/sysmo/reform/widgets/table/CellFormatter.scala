package sysmo.reform.widgets.table

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.data.Value.implicits._

import java.util.Date

trait CellFormatter {
  def render(v: Value): VdomElement = {
    render_formatted(
      format(v)
    )
  }
  def format(v: Value): Option[String]
  def format_na: String = "---"
  def render_formatted(v_formatted: Option[String]): VdomElement = {
    v_formatted match {
      case Some(x) => <.span(x)
      case None => <.span(format_na)
    }
  }
}

object IdCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[ObjectId].map(_.show)
}

object TextCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[String]
}

object DateCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] =
    v.get[Date].map(date =>
      (new scala.scalajs.js.Date(date.getTime)).toDateString()
    )
}

object LinkCellFormatter extends CellFormatter {
  override def format(v: Value): Option[String] = None
  override def render(v: Value): VdomElement = {
    case class Props()
    if (v.is_set) {
      <.i(^.cls:="fa-solid fa-link")
    } else {
      <.span(format_na)
    }
  }
}