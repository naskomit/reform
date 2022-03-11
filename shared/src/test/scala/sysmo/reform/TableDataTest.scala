package sysmo.reform

class TableDataTest {
  import sysmo.reform.shared.data.{table => sdt}
  import sysmo.reform.shared.util.pprint
  def basic_tests() = {
    import sdt.default.implicits._
    import sdt.Printers._
    import sdt.Transport._
    sdt.with_table_manager() { tm =>
      val v3_seq = Seq(
        tm.vec_from(Seq(1.0, 2.0, 4.0), "v3_real"),
        tm.vec_from(Seq(1, 2, 4), "v3_real"),
        tm.vec_from(Seq(true, true, false), "v3_real"),
        tm.vec_from(Seq("John", "Peter", "James"), "v3_real")
      )
      val schema = sdt.Schema(Seq(
        sdt.Field("real", sdt.FieldType(sdt.VectorType.Real)),
        sdt.Field("int", sdt.FieldType(sdt.VectorType.Int)),
        sdt.Field("bool", sdt.FieldType(sdt.VectorType.Bool)),
        sdt.Field("char", sdt.FieldType(sdt.VectorType.Char))
      ))
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
      println(pprint.pprint(tbl_1))
      println("==================== Test Vector Serialization ====================")
      //      sdt.Transport.round_trip(s1)
      println("==================== Test Table Serialization ====================")
      sdt.Transport.round_trip(tbl_1)

    }
  }
}
