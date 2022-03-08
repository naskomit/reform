package sysmo.coviddata

import java.nio.charset.StandardCharsets

import org.apache.arrow.vector.VectorSchemaRoot

import scala.jdk.CollectionConverters._
import scala.util.Using

object TestApp extends App {
//  CSVDataSource.test_read_write_csv()
//  SQLiteAppStorage.test_import()
//  SQLiteAppStorage.test_query()
//  SQLiteAppStorage.test_task

//  OrientDBGraphAppStorage.test_import()
    OrientDBGraphAppStorage.query_data()
//  ExcelReader.test1()



  def test_sysmo_table() = {
    import sysmo.reform.data.table.{arrow => dt}
//    import sysmo.reform.shared.data.table.{default => dt}
    import sysmo.reform.shared.data.{table => sdt}
    sdt.with_table_manager(dt.create_table_manager) { tm => {
      import dt.implicits._
      import sdt.Printers._
      val b1 = tm.incremental_vector_builder[Double]("v1")
      b1.append(Some(1.0))
      b1.append(None)
      b1 ++= Seq(3.0, 5.0, 8.0).map(Some(_))
      val v1 = b1.toVector
      println(v1)
      println(v1.map2(x => 2 * x))
      val v2 = v1.range(1, 1)
      println(v2)

      v1.close()
      v2.close()

      val v3_seq = Seq(
        tm.vec_from(Seq(1.0, 2.0, 4.0), "v3_real"),
        tm.vec_from(Seq(1, 2, 4), "v3_real"),
        tm.vec_from(Seq(true, true, false), "v3_real"),
        tm.vec_from(Seq("John", "Peter", "James"), "v3_real")
      )
//      v3_seq.foreach(v => {
//        println(v)
//        v.close()
//      })

      // Testing series
      val bs_1 = tm.incremental_series_builder(sdt.Field("bs_1", sdt.FieldType(sdt.VectorType.Real)))
      bs_1 :+ Some(3.0)
      bs_1 :+ None
      bs_1 :++ Seq(4.0, 5.0).map(Some(_))
      val s1 = bs_1.toSeries
      println(s1)

      // Testing table
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
      println(sdt.pprint(tbl_1))

      // JSON encoding/decoding
      println(s1)
      import io.circe.syntax._
      import sdt.Transport._

      println("==================== Test Vector Serialization ====================")
      round_trip(s1)
      println("==================== Test Table Serialization ====================")
      round_trip(tbl_1)
//      val s1_json = s1.asJson
//      val s1_back = s1_json.as[sdt.Series]
//      println(s1)
//      println(s1_json)
//      println(s1_back)



    }}
  }


  test_sysmo_table()
}


