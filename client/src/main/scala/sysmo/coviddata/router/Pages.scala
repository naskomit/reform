package sysmo.coviddata.router

import sysmo.coviddata.{panels => P}
import sysmo.reform.router.{Category, Page, PageBase, PageCollection}

case object HomePage extends Page {
  def name = "Home"
  def icon = "fa fa-desktop"
  def panel = P.HomePanel
}

case object Plots extends Page {
  def name = "Plots"
  def icon = "fa fa-pencil"
  def panel = P.PlotPanel
}


case object ClinicalData extends Page {
  def name = "ClinicalData"
  override def label = "Clinical Data"
  def icon = "fa-database"
  def panel = P.ClinicalDataPanel
}

case object SocioEconomicData extends Page {
  def name = "SocioEconomicData"
  override def label = "Socio-Economic Data"
  def icon = "fa-database"
  def panel = P.ClinicalDataPanel
}

case object ImmunologyData extends Page {
  def name = "ImmunologyData"
  override def label = "Immunology Data"
  def icon = "fa-database"
  def panel = P.ClinicalDataPanel
}

case object CovidData extends Category {
  override def children: Seq[PageBase] =
    Seq(ClinicalData, SocioEconomicData, ImmunologyData)
  override def name: String = "Covid Data"
  override def icon: String = "fa fa-database"
}

object Pages {
  val collection = PageCollection(
    HomePage,
    CovidData,
    Plots
  )
}