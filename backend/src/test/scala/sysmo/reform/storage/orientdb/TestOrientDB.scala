package sysmo.reform.storage.orientdb

import cats.MonadThrow
import com.typesafe.config.ConfigFactory
import kantan.csv.{DecodeError, rfc}
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.logging.Printer
import sysmo.reform.shared.runtime.LocalRuntime
import sysmo.reform.shared.util.Injector
import sysmo.reform.shared.util.containers.FLocal
import sysmo.reform.storage.io.csv
import sysmo.reform.storage.orientdb.TestOrientDB.input_reader

object TestOrientDB extends App {
  val printer = new Printer {
    override def out(msg: String): Unit = println(msg)
    override def warn(msg: String): Unit = println(msg)
    override def error(msg: String): Unit = println(msg)
  }
  Injector.configure(printer)

  val conf = ConfigFactory.load()
  val storage = sysmo.reform.storage.create_orientdb(conf.getConfig("storage.orientdb"))
  val session = storage.session
  val schema_service = session.schema
  val mt = MonadThrow[FLocal]
  val runtime = LocalRuntime()

  println(schema_service.list_schemas.map(s => s.name))

  val builder = SkullInventoryBuilder.type_builder
  schema_service.add_record_schema(builder.SkullSample)

  //  session.with_transaction {
  //  }

  val input_reader = new csv.Reader(
    "/data/Workspace/SysMo/re-form/data/import/Metadata.csv",
    builder.SkullSample,
    rfc.withHeader
  ).map_field(
    "image_type" -> "Image type"
  )

  input_reader.read { row_obj: input_reader.RowObj =>
    println((0 until 9).map(row_obj.get))
    mt.pure()
  } match {
    case Left(err) => throw err
    case Right(x) => x
  }

}
