package sysmo.reform.shared.tests

object TableDataTest {

  import sysmo.reform.shared.data.{table => sdt}
  import sysmo.reform.shared.util.pprint

  def basic_tests(): Unit = {
    import sdt.Printers._
    import sdt.Transport._
    import sdt.default.implicits._
    sdt.with_table_manager() { tm =>
      val v3_seq = Seq(
        tm.vec_from(Seq(1.0, 2.0, 4.0), "v3_real"),
        tm.vec_from(Seq(1, 2, 4), "v3_int"),
        tm.vec_from(Seq(true, true, false), "v3_bool"),
        tm.vec_from(Seq("John", "Peter", "James"), "v3_char")
      )
      val schema = sdt.Schema.builder("t1")
        .real("real")
        .int("int")
        .bool("bool")
        .char("char")
        .build

      val tb_1 = tm.incremental_table_builder(schema)
      for (i <- 0 until 3) {
        var row_data = Map(
          "real" -> v3_seq(0)(i), "int" -> v3_seq(1)(i),
          "bool" -> v3_seq(2)(i), "char" -> v3_seq(3)(i)
        )
        tb_1 :+ row_data
      }

      tb_1 :+ Map("real" -> None, "int" -> None, "bool" -> None, "char" -> None)
      val tbl_1 = tb_1.toTable
      tbl_1.pprint()
      println("==================== Test Vector Serialization ====================")
      println("==================== Test Table Serialization ====================")
//      sdt.Transport.round_trip(tbl_1)

    }
  }

  def group_test(): Unit = {
    sdt.with_table_manager() { tm =>
      val schema = sdt.Schema.builder("t1")
        .real("real")
        .int("int")
        .bool("bool")
        .char("char")
        .build

      val data = sdt.table_from_columns(
        tm, schema,
        "real" -> Seq(1.0, 2.0, 4.0, 8.0, 16.0, 32.0),
        "int" -> Seq(1, 2, 4, 8, 16, 32),
        "bool" -> Seq(true, true, false, true, false, false),
        "char" -> Seq("John", "Peter", "James", "John", "James", "John"),
      )

      data.pprint()

      val grouped = data.group_by(row => row.get("char"))
      for ((k, v) <- grouped) {
        println(k)
        v.pprint()
      }

    }
  }
}
