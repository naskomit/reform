package sysmo.reform.util

import io.circe.Json

import scala.scalajs.js

object json {
  def circe_2_js(x: Json): js.Object = js.JSON.parse(x.spaces2).asInstanceOf[js.Object]
}
