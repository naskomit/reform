package sysmo.coviddata

import java.nio.charset.StandardCharsets

import scala.jdk.CollectionConverters._
import scala.util.Using

object TestApp extends App {
//  CSVDataSource.test_read_write_csv()
//  SQLiteAppStorage.test_import()
//  SQLiteAppStorage.test_query()
//  SQLiteAppStorage.test_task

//  OrientDBGraphAppStorage.test_import()
//  ExcelReader.test1()

  import sysmo.reform.data.{table => dt}
  import sysmo.reform.shared.data.{table => sdt}
  def test_sysmo_table() = {
    Using(dt.ArrowTableManager()) { tm => {
      import sysmo.reform.data.table.ArrowVector._
      val b1 = tm.incremental_vector_builder[Double]("v1")

      b1.append(1.0)
      b1 ++= Seq(3.0, 5.0, 8.0)
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
      v3_seq.foreach(v => {
        println(v)
        v.close()
      })
    }}.get
  }


//  test_arrow()
  test_sysmo_table()
}


