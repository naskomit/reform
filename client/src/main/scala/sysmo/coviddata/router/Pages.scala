package sysmo.coviddata.router

sealed trait Pages {
  def name : String
}

case object HomePage extends Pages {
  def name : String = "Home"
}

case object Form1Page extends Pages {
  def name : String = "Form1"
}