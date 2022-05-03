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

  def read_clinical_lab = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.ClinicalLab.schema)
      .build

    def maybe_v(x: String)(value: sdt.Value[_]): Boolean = value.is_na || value.as_char.get == x

    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("day").shift()
      .shift().check_char_value(maybe_v("Xr")).shift_read("Xr")
      .shift().check_char_value(maybe_v("Леук")).shift_read("Леук")
      .shift().check_char_value(maybe_v("Неутр")).shift_read("Неутр")
      .shift().check_char_value(maybe_v("Мо")).shift_read("Мо")
      .shift().check_char_value(maybe_v("Тром")).shift_read("Тром")
      .shift().check_char_value(maybe_v("Урея")).shift_read("Урея")
      .shift().check_char_value(maybe_v("СУЕ")).shift_read("СУЕ")
      .shift().check_char_value(maybe_v("СРП")).shift_read("СРП")
      .shift().check_char_value(maybe_v("креатинин")).shift_read("Креатинин")
      .shift().check_char_value(maybe_v("Пик.к-на")).shift_read("Пик.к-на")
      .shift().check_char_value(maybe_v("АСАТ")).shift_read("АСАТ")
      .shift().check_char_value(maybe_v("АЛАТ")).shift_read("АЛАТ")
      .shift().check_char_value(maybe_v("Фибр.")).shift_read("Фибр.")
      .shift().check_char_value(maybe_v("Кр.захар")).shift_read("Кр.захар")
      .shift().check_char_value(maybe_v("Д.димер")).shift_read("Д.димер")
      .shift().check_char_value(maybe_v("тропонин")).shift_read("Тропонин")
      .shift().check_char_value(maybe_v("ЛДХ")).shift_read("ЛДХ")
      .shift().check_char_value(maybe_v("СК")).shift_read("СК")
      .shift().check_char_value(maybe_v("СК..МБ")).shift_read("СК..МБ")
      .shift().check_char_value(maybe_v("INR")).shift_read("INR")
      .shift().check_char_value(maybe_v("ПВ%")).shift_read("ПВ%")
      .shift().check_char_value(maybe_v("Феритин")).shift_read("Феритин")
      .shift().check_char_value(maybe_v("Прокалцитонин")).shift_read("Прокалцитонин")
      .shift().check_char_value(maybe_v("Интерлевкин 6")).shift_read("Интерлевкин 6")
      .shift().check_char_value(maybe_v("O2 сатурация")).shift_read("O2 сатурация")
      .shift().check_char_value(maybe_v("Калий")).shift_read("Калий")
      .shift().check_char_value(maybe_v("Натрий")).shift_read("Натрий")
      .shift().check_char_value(maybe_v("Хлор")).shift_read("Хлор")
      .shift().check_char_value(maybe_v("Калций")).shift_read("Калций")
      .shift().check_char_value(maybe_v("Газов анализ")).shift_read("pH")
      .shift().check_char_value(maybe_v("КАР")).shift_read("pCO2")
      .shift().check_char_value(maybe_v("КАР")).shift_read("pO2")
      .shift().check_char_value(maybe_v("общ бел")).shift_read("общ бел")
      .shift().check_char_value(maybe_v("алб")).shift_read("алб")
      .build

    ReadBlock(
      schema,
      "Част 4-Клин.III",
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

  def read_therapy_1 = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Therapy1.schema)
      .build

    val read_row = ReadRow.multi_column_block_builder(schema)
      .fixed(b =>
        b.read("1").shift_read("1a").shift_read("1b").shift_read("1c").shift()
      ).variable(20, b =>
        b.shift_read("kind")
        .shift_read("start").shift_read("end")
        .shift_read("duration")
        .shift_read("dose")
    ).build

    ReadBlock(
      schema,
      "Част 6-Терап.",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_therapy_2 = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Therapy2.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("45").shift_2_read("45a")
      .shift_2_read("45b").shift_2_read("45c")
      .shift_2_read("46").shift_2_read("47")
      .shift_2_read("48").shift_2_read("49")
      .shift_2_read("50")
      .build

    ReadBlock(
      schema,
      "Част 7-Терап.II",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_therapy_lab = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.TherapyLab.schema)
      .build

    def maybe_v(x: String)(value: sdt.Value[_]) = value.is_na || value.as_char.get == x

    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("event").shift_read("date").shift()
      .shift().check_char_value(maybe_v("Xr")).shift_read("Xr")
      .shift().check_char_value(maybe_v("Леук")).shift_read("Леук")
      .shift().check_char_value(maybe_v("Неутр")).shift_read("Неутр")
      .shift().check_char_value(maybe_v("Мо")).shift_read("Мо")
      .shift().check_char_value(maybe_v("Тром")).shift_read("Тром")
      .shift().check_char_value(maybe_v("Урея")).shift_read("Урея")
      .shift().check_char_value(maybe_v("СУЕ")).shift_read("СУЕ")
      .shift().check_char_value(maybe_v("СРП")).shift_read("СРП")
      .shift().check_char_value(maybe_v("креатинин")).shift_read("Креатинин")
      .shift().check_char_value(maybe_v("Пик.к-на")).shift_read("Пик.к-на")
      .shift().check_char_value(maybe_v("АСАТ")).shift_read("АСАТ")
      .shift().check_char_value(maybe_v("АЛАТ")).shift_read("АЛАТ")
      .shift().check_char_value(maybe_v("Фибр.")).shift_read("Фибр.")
      .shift().check_char_value(maybe_v("Кр.захар")).shift_read("Кр.захар")
      .shift().check_char_value(maybe_v("Д.димер")).shift_read("Д.димер")
      .shift().check_char_value(maybe_v("тропонин")).shift_read("Тропонин")
      .shift().check_char_value(maybe_v("ЛДХ")).shift_read("ЛДХ")
      .shift().check_char_value(maybe_v("СК")).shift_read("СК")
      .shift().check_char_value(maybe_v("СК..МБ")).shift_read("СК..МБ")
      .shift().check_char_value(maybe_v("INR")).shift_read("INR")
      .shift().check_char_value(maybe_v("ПВ%")).shift_read("ПВ%")
      .shift().check_char_value(maybe_v("Феритин")).shift_read("Феритин")
      .shift().check_char_value(maybe_v("Прокалцитонин")).shift_read("Прокалцитонин")
      .shift().check_char_value(maybe_v("Интерлевкин 6")).shift_read("Интерлевкин 6")
      .shift().check_char_value(maybe_v("O2 сатурация")).shift_read("O2 сатурация")
      .shift().check_char_value(maybe_v("Калий")).shift_read("Калий")
      .shift().check_char_value(maybe_v("Натрий")).shift_read("Натрий")
      .shift().check_char_value(maybe_v("Хлор")).shift_read("Хлор")
      .shift().check_char_value(maybe_v("Калций")).shift_read("Калций")
      .shift().check_char_value(maybe_v("Газов анализ")).shift_read("Газов анализ")
      .shift().check_char_value(maybe_v("КАР")).shift_read("КАР")
      .shift().check_char_value(maybe_v("кислород")).shift_read("Кислород")
      .build

    ReadBlock(
      schema,
      "Част 8-Терап.III",
      PositionAt(4, 0),
      read_row
    )
  }

  def read_immunology = {
    val schema: T.Schema = G.Schema
      .table_schema_builder(cd.Immunology.schema)
      .build
    val read_row = ReadRow.builder(schema)
      .read("1").shift_read("1a").shift_read("1b").shift_read("1c")
      .shift_read("2").col("P").read("3")
      .col("BH").read("day")
      .shift_read("25").shift_2_read("26")
      .shift_2_read("27").shift_2_read("28")
      .shift_2_read("29").shift_2_read("30")
      .shift_2_read("31").shift_2_read("32")
      .shift_2_read("33").shift_2_read("34")
      .shift_2_read("35").shift_2_read("36")
      .shift_2_read("37").shift_2_read("38")
      .shift_2_read("39").shift_2_read("40")
      .shift_2_read("41").shift_2_read("42")
      .build

    ReadBlock(
      schema,
      "Част 9-Имун.",
      PositionAt(4, 0),
      read_row
    )
  }

}
