package sysmo.reform.storage.io.csv

import cats.MonadThrow
import kantan.csv.CsvConfiguration
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.{RecordFieldInstance, RecordInstance}
import sysmo.reform.shared.table.Table
import sysmo.reform.shared.table.Table.Schema
import sysmo.reform.shared.types.{ArrayType, PrimitiveDataType, CompoundDataType, MultiReferenceType, RecordType, ReferenceType}
import sysmo.reform.shared.util.MonadicIterator
import sysmo.reform.shared.util.containers.FLocal

import java.io.File
import scala.util.Using

class Reader(path: String, rec_type: RecordType, conf: CsvConfiguration) {
  import kantan.csv._ // All kantan.csv types.
  import kantan.csv.ops._ // Enriches types with useful methods.
  type F[+X] = FLocal[X];
  type RowObj = Table.Row

  private var header_mappings: Map[String, String] = Map()

  def map_field(mp: (String, String)*): this.type = {
    mp.foreach {
      mp => header_mappings = header_mappings + mp
    }
    this
  }

  def parse_value[T](raw: String)(implicit cd: CellDecoder[T]): Value = {
    val v = cd.decode(raw)
    v match {
      case Left(value) => Value.empty
      case Right(value) => Value.from_any(value)
    }
  }

  class RDecoder(field_indices: Seq[Int]) extends RowDecoder[RowObj] {
    override def decode(e: Seq[String]): Either[DecodeError, RowObj] = {
      Right(new Table.Row {
        override def schema: Schema = rec_type
        override protected def _get(col: Int): Value = {
          val dtype = rec_type.fields(col).dtype
          val raw_value = e(field_indices(col))
          dtype match {
            case dt: PrimitiveDataType => dt match {
              case PrimitiveDataType.Real => parse_value[Double](raw_value)
              case PrimitiveDataType.Int => parse_value[Int](raw_value)
              case PrimitiveDataType.Long => parse_value[Long](raw_value)
              case PrimitiveDataType.Char => {
                import Value.implicits._
                val v = parse_value[String](raw_value)
                if (v.get[String].contains("")) {
                  Value.empty
                } else v
              }
              case PrimitiveDataType.Bool => parse_value[Boolean](raw_value)
              case PrimitiveDataType.Date => Value.empty
              case PrimitiveDataType.Id => parse_value[Double](raw_value)
            }
          }
        }
      })
    }
  }

  implicit val hd: HeaderDecoder[RowObj] = new HeaderDecoder[RowObj] {
    override def fromHeader(header: Seq[String]): DecodeResult[RowDecoder[RowObj]] = {
      val header_map = header.zipWithIndex.toMap
      val field_indices: DecodeResult[Seq[Int]] = rec_type.fields.foldLeft[DecodeResult[Seq[Int]]](Right(Seq()))((acc, item) => {
        acc match {
          case Right(s) => header_map.get(header_mappings.getOrElse(item.name, item.name)) match {
            case Some(ind) => Right(s :+ ind)
            case None => Left(DecodeError.TypeError(s"Field ${item.name} not present in the input file"))
          }
          case err@Left(_) => err
        }
      })
      field_indices
        .map(fi => new RDecoder(fi))

    }

    override def noHeader: RowDecoder[RowObj] = ???
  }


  def read(f: RowObj => FLocal[Unit]): FLocal[Unit] = {
    Using(new File(path).asCsvReader[RowObj](conf)) {
      reader => reader.map {
        case Left(e) => throw new RuntimeException("Failed creating a reader", e)
        case Right (x) => x
      }.foreach(row => f(row))
    }.toEither
  }

}
