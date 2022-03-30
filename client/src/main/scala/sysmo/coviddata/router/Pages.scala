package sysmo.coviddata.router

import sysmo.coviddata.panels.TablePanels
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
  def panel = P.ChartPanel
}


case object SocioDemographicData extends Page {
  def name = "SocioDemographicData"
  override def label = "Socio-Demographic Data"
  def icon = "fa-database"
  def panel = TablePanels.SocioDemographic
}

case object ClinicalData extends Page {
  def name = "ClinicalData"
  override def label = "Clinical Data"
  def icon = "fa-database"
  def panel = TablePanels.Clinical
}

case object TherapyData extends Page {
  def name = "TherapyData"
  override def label = "Therapy Data"
  def icon = "fa-database"
  def panel = TablePanels.Therapy
}

case object ImmunologyData extends Page {
  def name = "ImmunologyData"
  override def label = "Immunology Data"
  def icon = "fa-database"
  def panel = TablePanels.Immunology
}

case object CovidData extends Category {
  override def children: Seq[PageBase] =
    Seq(SocioDemographicData, ClinicalData, TherapyData, ImmunologyData)
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