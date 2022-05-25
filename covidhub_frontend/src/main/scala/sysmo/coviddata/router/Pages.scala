package sysmo.coviddata.router

import sysmo.coviddata.{panels => P}
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

case object Analysis extends Page {
  val name = "Analysis"
  val label = Some("Analysis")
  val icon = "fa fa-pencil"
  val panel = P.RefrigerationCyclePanel
}


//case object SocioDemographicData extends Page {
//  def name = "SocioDemographicData"
//  override def label = "Socio-Demographic Data"
//  def icon = "fa-database"
//  def panel = TablePanels.SocioDemographic
//}
//
//case object ClinicalData extends Page {
//  def name = "ClinicalData"
//  override def label = "Clinical Data"
//  def icon = "fa-database"
//  def panel = TablePanels.Clinical
//}
//
//case object TherapyData extends Page {
//  def name = "TherapyData"
//  override def label = "Therapy Data"
//  def icon = "fa-database"
//  def panel = TablePanels.Therapy
//}
//
//case object ImmunologyData extends Page {
//  def name = "ImmunologyData"
//  override def label = "Immunology Data"
//  def icon = "fa-database"
//  def panel = TablePanels.Immunology
//}

//case class CovidData(app_config: ApplicationConfiguration) extends Category {
//  override def children: Seq[PageBase] = TablePanels.builder(app_config)
//    .build
////    Seq(SocioDemographicData, ClinicalData, TherapyData, ImmunologyData)
//
//  override def name: String = "Covid Data"
//  override def icon: String = "fa fa-database"
//}

//object Pages {
//  def create(app_config: ApplicationConfiguration) =
//    PageCollection(
//      HomePage,
//      CovidData(app_config),
//      Plots
//    )
//}