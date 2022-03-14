package sysmo.coviddata.io

import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.shared.util.pprint
import sysmo.coviddata.shared.{data => cd}

object ExcelImporter {
  import sysmo.reform.io.excel._
  import sysmo.reform.shared.data.{graph => G}
  import sysmo.reform.shared.data.{table => T}

  def read_sociodemographic = {
    val schema : T.Schema = G.Schema
      .table_schema_builder(cd.SocioDemographic.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a")
      .shift_read("1b").shift_read("1c")
      .shift_read("1d").shift_2_read("3")
      .shift_2_read("4a").shift_2_read("4")
      .shift_2_read("5").shift_2_read("5a")
      .shift_2_read("5b").shift_2_read("5c")
      .shift_2_read("5d").shift_2_read("6")
      .shift_2_read("7").shift_2_read("8")
      .shift_2_read("9").shift_2_read("10")
      .shift_2_read("11").shift_2_read("12")
      .shift_2_read("13").shift_2_read("14a")
      .shift_2_read("14a1").shift_read("14b")
      .shift_2_read("14b1").shift_2_read("14c")
      .shift_2_read("14d").shift_2_read("14e")
      .shift_2_read("14f")
      .build

    ReadBlock(
      schema,
      "Част 1-СДХ",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_clinical_1 = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Clinical.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_2_read("14").shift_read("14a")
      .shift_2_read("15").shift_2_read("16").shift_2_read("17")
      .shift_2_read("18").shift_2_read("19a").shift_2_read("19b")
      .shift_2_read("19c").shift_2_read("19d").shift_2_read("19e")
      .shift_2_read("19f").shift_2_read("19g").shift_2_read("19h")
      .shift_2_read("19i").shift_2_read("19j").shift_2_read("19k")
      .shift_2_read("19l").shift_2_read("20a").shift_2_read("20b")
      .shift_2_read("21").shift_2_read("22a").shift_2_read("22b")
      .shift_2_read("23").shift_2_read("24a").shift_2_read("24b")
      .shift_2_read("24c").shift_2_read("24d").shift_2_read("25")
      .shift_2_read("26").shift_2_read("27").shift_2_read("28")
      .shift_2_read("29").shift_2_read("30a").shift_2_read("30b")
      .shift_2_read("30c")
      .build

    ReadBlock(
      schema,
      "Част 2 - Клин.I",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_clinical_2 = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Clinical.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("31").shift_2_read("32")
      .shift_2_read("33").shift_2_read("34")
      .shift_2_read("35a").shift_read("35a1")
      .shift_read("35b").shift_read("35b1")
      .shift_read("35c").shift_read("35c1")
      .shift_read("35d").shift_read("35d1")
      .shift_2_read("36").shift_read("36a")
      .shift_2_read("36b").shift_2_read("36c")
      .shift_2_read("36d").shift_2_read("36e")
      .shift_2_read("36f").shift_2_read("36g")
      .shift_2_read("37a").shift_2_read("37b")
      .shift_2_read("37c").shift_2_read("37d")
      .shift_2_read("37e").shift_2_read("37f")
      .build

    ReadBlock(
      schema,
      "Част 3-Клин.II",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_clinical_4 = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Clinical.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("39a").shift_2_read("39b")
      .shift_2_read("40a").shift_2_read("40b")
      .shift_2_read("41a").shift_2_read("41b")
      .shift_2_read("42a").shift_2_read("42b")
      .shift_2_read("43a").shift_2_read("43b")
      .shift_2_read("44a").shift_2_read("44b")
      .build

    ReadBlock(
      schema,
      "Част 5-Клин.V",
      PositionAt(4, 0),
      read_row
    )
  }
  import sdt.Printers._

  def test1(): Unit = {
    val doc_path = "doc/SampleData_3.xlsx"
    sdt.with_table_manager()(tm => {
      val reader = new WorkbookReader(doc_path, tm)
      val data = reader.read_table_collection(TableCollectionRead(Map(
        "SocioDemographic" -> read_sociodemographic,
//        "Clinical_1" -> read_clinical_1
      )))
      data.foreach {case (name, tbl) => {
        println(f"================= $name ================")
        pprint.pprint(tbl)
      }}
    })
  }
}
