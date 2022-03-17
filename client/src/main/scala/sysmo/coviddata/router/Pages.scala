package sysmo.coviddata.router

import sysmo.coviddata.panels.{HomePanel, PlotPanel}
import sysmo.reform.router.{Category, Page, PageBase, PageCollection}

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


case object Subcat1 extends Page {
  def name = "Subcat1"
  def icon = ""
  def panel = PlotPanel
}

case object Subcat2 extends Page {
  def name = "Subcat2"
  def icon = ""
  def panel = PlotPanel
}

case object Cat1 extends Category {
  override def children: Seq[PageBase] =
    Seq(Subcat1, Subcat2)
  override def name: String = "Cat1"
  override def icon: String = "fa fa-bath"
}

object Pages {
  val collection = PageCollection(
    HomePage,
    Plots ,
    Cat1
  )
}