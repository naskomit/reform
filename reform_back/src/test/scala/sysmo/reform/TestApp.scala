package sysmo.reform

import sysmo.coviddata.OrientDBGraphAppStorage
import sysmo.reform.query.GremlinTest
import sysmo.reform.util.Logging

import scala.util.Using

object TestApp extends App with Logging {
  def fix_names() = {
    logger.info("Fixing names")
    val name_map = Map(
      95 -> Seq("Иван", "Петров", "Стоянов"),
      96 -> Seq("Станимира", "Иванова", "Димитрова"),
      120 -> Seq("Пепа", "Кънчева", "Иванова"),
      121 -> Seq("Христина", "Иванова", "Христова"),
      123 -> Seq("Дуда", "Христова", "Николова"),
      162 -> Seq("Владислава", "Николаева", "Господинова"),
      171 -> Seq("Пенка", "Иванова", "Веселинова"),
      172 -> Seq("Пресияна", "Руменова", "Колева"),
      173 -> Seq("Гена", "Павлова", "Бабанска"),
      174 -> Seq("Марияна", "Петрова", "Ласкина"),
      175 -> Seq("Клавдий", "Гунев", "Цоцолски"),
      176 -> Seq("Илия", "Станчов", "Гърмидолов"),
      183 -> Seq("Гатьо", "Веселинов", "Гатев"),
      208 -> Seq("Хасан", "Самуилов", "Ахмедов"),
    )

    OrientDBGraphAppStorage.app_storage.transactional(graph => {
      val g = graph.traversal
      for (item <- name_map) {
        g.V().has("1", item._1)
          .property("1a", item._2(0))
          .property("1b", item._2(1))
          .property("1c", item._2(2))
          .iterate()
      }


    })


  }

  def do_import(): Unit = {
    OrientDBGraphAppStorage.test_import()
    fix_names()
    logger.info("Finished import!")
  }



  //    sysmo.reform.data.table.TestTable.test_sysmo_table()

    import sysmo.coviddata.OrientDBGraphAppStorage
//  OrientDBGraphAppStorage.test_import()
//  OrientDBGraphAppStorage.query_data()
//    OrientDBGraphAppStorage.test_query_table()

//    import sysmo.reform.query.QueryTest
//    QueryTest.test_serialization()

//    sysmo.coviddata.io.ExcelImporter.test1()
  import sysmo.reform.plots.TestPlotly
//  TestPlotly.test_bar()
//  sysmo.typelevel.JsonCats.test1()
//  sysmo.typelevel.ScalaWithCats.run()

  def test_gremlin(): Unit = {
    val setup = new GremlinTest(OrientDBGraphAppStorage.factory)
    setup.run()
  }

  def run(): Unit = {
    do_import()
//    test_gremlin()
  }

  run()
}
