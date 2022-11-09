package sysmo.reform.shared.table

import io.circe.Decoder.{Result, failed}
import sysmo.reform.shared.util.CirceTransport
import cats.implicits._
import io.circe.{Codec, DecodingFailure}
import sysmo.reform.shared.data.Value
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, MultiReferenceType, PrimitiveDataType, ReferenceType}

object Transport extends CirceTransport {

  import io.circe.syntax._
  import sysmo.reform.shared.data.Transport._
  import sysmo.reform.shared.types.Transport._

  implicit val enc_row: Encoder[Table.Row] =
    Encoder.instance{row =>
      Json.fromValues(
        (0 until row.ncol)
          .map(i => row.get(i).asJson)
      )
    }

  val enc_LocalTable: Encoder[LocalTable] =
    Encoder.instance{table =>
      Map(
        "schema" -> table.schema.asJson,
        "values" -> Json.fromValues(table.iterator.map(_.asJson).toSeq)
      ).asJson

    }

  class RowDecoder(schema: Table.Schema, field_decoders: Seq[Decoder[Value]]) extends Decoder[Table.Row] {
    override def apply(c: HCursor): Result[Table.Row] = {
      field_decoders.zipWithIndex.traverse {
        case (field_decoder, index) => c.downN(index).as[Value](field_decoder)
      }.map(values => Table.Row.SeqRow(schema, values))
    }
  }

  object LocalTableDecoder extends Decoder[LocalTable] {
    override def apply(c: HCursor): Result[LocalTable] = {
      for {
        schema <- c.downField("schema").as[Table.Schema]

        row_decoder <- schema.fields.traverse {field => {
            field.dtype match {
              case prim_dtype: PrimitiveDataType => Right(new ValueDecoder(prim_dtype))
              case _ => Left(DecodingFailure(s"Currently TableDecoder can only decode primitive values", c.history))
            }
          }
        }.map(field_decoders => new RowDecoder(schema, field_decoders))

        rows <- {
          implicit val dec_TableRow: Decoder[Table.Row] = row_decoder
          c.downField("values").as[Seq[Table.Row]]
        }
      } yield LocalRowBasedTable(schema, rows)
    }
  }

  implicit val codec_LocalTable: Codec[LocalTable] = Codec.from(
    LocalTableDecoder, enc_LocalTable
  )

}
