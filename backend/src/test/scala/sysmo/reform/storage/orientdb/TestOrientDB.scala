package sysmo.reform.storage.orientdb

import cats.MonadThrow
import cats.syntax.all._
import com.typesafe.config.ConfigFactory
import kantan.csv.{DecodeError, rfc}
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.logging.Printer
import sysmo.reform.shared.runtime.LocalRuntime
import sysmo.reform.shared.table.{BasicQuery, Fields, SingleTable}
import sysmo.reform.shared.types.{RecordType, TypeSystem}
import sysmo.reform.shared.util.Injector
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.storage.io.csv

object TestOrientDB extends App {
  val printer = new Printer {
    override def out(msg: String): Unit = println(msg)
    override def warn(msg: String): Unit = println(msg)
    override def error(msg: String): Unit = println(msg)
  }
  Injector.configure(printer)

  val ts: TypeSystem = SkullInventoryBuilder.type_builder.build
  val SkullEntry = ts.get("SkullEntry").get.asInstanceOf[RecordType]

  val conf = ConfigFactory.load()
  val storage = sysmo.reform.storage.create_orientdb[FLocal](
    conf.getConfig("storage.orientdb")
  )
  val session = storage.session
  val schema_service = session.schema
  val runtime = session.runtime(ts)

  val mt = MonadThrow[FLocal]


  def import_data(): Unit = {
    schema_service.sync(ts).onError {
      case e: Throwable => throw(e)
    }

    val input_reader = new csv.Reader(
      "data/import/Metadata.csv",
      SkullEntry,
      rfc.withHeader
    ).map_field(
      "image_type" -> "Image type"
    )

    input_reader.read { row_obj: input_reader.RowObj =>
      for {
        rec <- runtime.create_record(SkullEntry, None)
        rec2 <- {
          val values = row_obj.schema.fields.zipWithIndex
            .map { case (field, index) =>
              (field.name, row_obj.get(index))
            }
          runtime.update_record(rec.id, values)
        }
      } yield rec2

    }.onError {
      case e: Throwable => throw(e)
    }
  }

  def query_data(): Unit = {
    // select @rid, code, sex, age, image_type, BMI, `Filter 1`, `Filter 2` from `SkullEntry`
    // ORDER BY BMI DESC
    // delete from `SkullEntry`
    val query = BasicQuery(
      source = SingleTable("SkullEntry"),
      projection = Fields(Seq("@rid", "code", "sex", "age", "image_type").map(f => Expression.field(f)))
    )

  }

  import_data()
//  query_data()
}
