package sysmo.smoskull

import cats.MonadThrow
import cats.syntax.all._
import kantan.csv.rfc
import sysmo.reform.server.OrientDBReformServer
import sysmo.reform.shared.containers.FLocal
import sysmo.reform.shared.containers.implicits._
import sysmo.reform.shared.examples.SkullInventoryBuilder
import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.query.{BasicQuery, Fields, SingleTable}
import sysmo.reform.shared.table.TablePrinter
import sysmo.reform.shared.types.{RecordType, TypeSystem}
import sysmo.reform.storage.io.csv

object SmoSkullApp {
  object SkullInventoryReformServer extends OrientDBReformServer[FLocal] {
    override val mt = MonadThrow[FLocal]
    override val type_system: TypeSystem = SkullInventoryBuilder.type_builder.build
  }

  val ts = SkullInventoryReformServer.type_system
  val SkullSampleType = ts.get("SkullSample").get.asInstanceOf[RecordType]
  val session = SkullInventoryReformServer.storage.session
  val schema_service = session.schema
  val qs = session.query_service
  val runtime = session.runtime(ts)


  // "apps/demo1/backend/data/Metadata_v2_20221115.csv"
  def import_data(input_file: String): Unit = {
    schema_service.sync(ts).onError {
      case e: Throwable => throw (e)
    }

    val input_reader = new csv.Reader(
      input_file,
      SkullSampleType,
      rfc.withHeader
    ).map_field(
//      "image_type" -> "Image type"
    )

    input_reader.read { row_obj: input_reader.RowObj =>
      for {
        rec <- runtime.create_record(SkullSampleType, None)
        rec2 <- {
          val values = row_obj.schema.fields.zipWithIndex
            .map { case (field, index) =>
              (field.name, row_obj.get(index))
            }
          runtime.update_record(rec.id, values)
        }
      } yield rec2

    }.onError {
      case e: Throwable => throw (e)
    }
  }

  def query_data(): Unit = {
    val fields = Seq("code", "sex", "age", "image_type")
      .map { name =>
        val ftype = SkullSampleType.field(name)
        Expression.field(name, ftype)
      }


    val query = BasicQuery(
      source = SingleTable("SkullSample"),
      projection = Fields(fields)
    )

    runtime.run_query(query).flatMap(_.cache)
      .map(tbl => println(new TablePrinter(tbl).print))
      .onError {
        case e: Throwable => throw (e)
      }

  }

  def main(args: Array[String]): Unit = {
    args.toSeq match {
      case Seq("import", args@_*) => args match {
        case Seq(path: String, _*) => import_data(path)
      }
      case Seq(x, _*) => throw new IllegalArgumentException(s"Unknown action $x")
      case _ => throw new IllegalArgumentException("Please specify action and arguments")
    }
  }

  //  import_data()
  //  query_data()
}
