package sysmo.coviddata.router

import sysmo.coviddata.panels.{HomePanel, PlotPanel}
import sysmo.reform.router.{Page, PageCollection}

case object HomePage extends Page {
  def name = "Home"
  def icon = "fa fa-desktop"
  def panel = HomePanel
}

case object Plots extends Page {
  def name = "Plots"
  def icon = "fa fa-pencil"
  def panel = PlotPanel
}

object Pages {
  val collection = PageCollection(
    HomePage,
    Plots
  )
}