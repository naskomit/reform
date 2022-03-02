package sysmo.coviddata

import java.nio.charset.StandardCharsets

import org.apache.arrow.memory.RootAllocator
import sysmo.reform.shared.data.table.{RealVector, TableManager}

import scala.jdk.CollectionConverters._
import scala.util.Using

object TestApp extends App {
//  CSVDataSource.test_read_write_csv()
//  SQLiteAppStorage.test_import()
//  SQLiteAppStorage.test_query()
//  SQLiteAppStorage.test_task

//  OrientDBGraphAppStorage.test_import()
//  ExcelReader.test1()

  def test_arrow() = {
    import org.apache.arrow.vector.{VarCharVector, IntVector, VectorSchemaRoot, FieldVector}
    import org.apache.arrow.memory.RootAllocator
    val allocator = new RootAllocator(Long.MaxValue)
    val v1 = new VarCharVector("name", allocator)
    val v2 = new IntVector("age", allocator)
    v1.allocateNew()
    v2.allocateNew()
    for (i <- 0 until 10) {
      v1.setSafe(i, f"Blah $i".getBytes(StandardCharsets.UTF_8))
      v2.setSafe(i, i * i)
    }
    v1.setValueCount(10)
    v2.setValueCount(10)

    val fields = Seq(v1.getField, v2.getField)
    val vectors = Seq[FieldVector](v1, v2)
    val schema_root = new VectorSchemaRoot(fields.asJava, vectors.asJava)

    println(schema_root.getRowCount)
    println()
  }

  import sysmo.reform.data.{table => dt}
  import sysmo.reform.shared.data.{table => sdt}
  def test_sysmo_table() = {
    Using(dt.ArrowTableManager()) { tm => {
      import sysmo.reform.data.table.Implicits._
      val b1 = tm.incremental_vector_builder[sdt.RealVector]("v1")
//      b1.append("asda")
      b1 ++= Seq(3.0, 5.0, 8.0)
      val v1 = b1.toVector
      println(v1)
      val v2 = v1.range(1, 1)
      println(v2)
      println(v1)
      println(v2)
      v1.close()
      v2.close()
    }}}


//  test_arrow()
  test_sysmo_table()
}


