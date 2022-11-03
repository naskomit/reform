package sysmo.reform.shared.table

import io.circe.Decoder.Result
import sysmo.reform.shared.util.CirceTransport

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

  implicit val enc_LocalTable: Encoder[LocalTable] =
    Encoder.instance{table =>
      Map(
        "schema" -> table.schema.asJson,
        "values" -> Json.fromValues(table.iterator.map(_.asJson).toSeq)
      ).asJson

    }

  class RawDecoder(schema: Table.Schema) extends Decoder[Table.Row] {
    override def apply(c: HCursor): Result[Table.Row] = ???
  }
}
