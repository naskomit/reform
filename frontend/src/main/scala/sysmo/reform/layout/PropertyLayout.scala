package sysmo.reform.layout

import sysmo.reform.react.ReactComponent
import sysmo.reform.react.property.PropertySourceView

trait PropertyLayout extends ReactComponent {
  import PropertyLayout._
  case class Props(source: PropertySourceView, options: RenderingOptions)

}

object PropertyLayout {
  sealed trait SizeHint
  object SizeHint {
    case object ExtraShort extends SizeHint
    case object Short extends SizeHint
    case object Medium extends SizeHint
    case object Long extends SizeHint
    case object ExtraLong extends SizeHint
    case object FullWidth extends SizeHint
  }

  trait RenderingOptions {
    def background_white: Boolean
  }

}