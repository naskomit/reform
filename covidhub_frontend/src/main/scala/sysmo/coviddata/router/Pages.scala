package sysmo.coviddata.router

import sysmo.coviddata.{panels => P}
import sysmo.reform.components.ApplicationPanel
import sysmo.reform.router.Page

case object HomePage extends Page {
  val name = "Home"
  val label = None
  val icon = "fa fa-desktop"
  val panel = P.HomePanel
}

case object Plots extends Page {
  val name = "Plots"
  val label = Some("Графики")
  val icon = "fa fa-pencil"
  val panel = P.ChartPanel
}
