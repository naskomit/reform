package sysmo.reform.shared.data.table

import cats.implicits._
import io.circe.Decoder.Result


object Transport {
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._
//  type TypedVector[V] = Vector[V, VectorStorage[V]]
//  type VectorEncoder[V] = Encoder[Vector[V, VectorStorage[V]]]
//
//  implicit val encode_vector: VectorEncoder[Double]  = new VectorEncoder[Double] {
//    override def apply(a: Vector[_, _]): Json = Json.JObject(
//      ("data", a.toVector.map())
//    )
//  }

  implicit val enc_value: Encoder[Value] = new Encoder[Value] {
    override def apply(a: Value): Json = {
      a.v match {
        case None => Json.Null
        case Some(x : Double) => Json.fromDoubleOrNull(x)
        case Some(x : Int) => Json.fromInt(x)
        case Some(x : Boolean) => Json.fromBoolean(x)
        case Some(x : String) => Json.fromString(x)
      }
    }
  }

  implicit val enc_field_type: Encoder[FieldType] = new Encoder[FieldType] {
    override def apply(a: FieldType): Json = Json.obj(
      "tpe" -> a.tpe.toString.asJson,
      "nullable" -> a.nullable.asJson,
      "metadata" -> a.metadata.asJson
    )
  }

  implicit val dec_field_type: Decoder[FieldType] = new Decoder[FieldType] {
    override def apply(c: HCursor): Result[FieldType] = for {
      tpe <- c.downField("tpe").as[String]
      nullable <- c.downField("nullable").as[Boolean]
      metadata <- c.downField("metadata").as[Map[String, String]]

    } yield FieldType(VectorType.withName(tpe), nullable, metadata)
  }

  implicit val codec_field: Codec[Field] = deriveCodec[Field]

  implicit val enc_series: Encoder[Series] = new Encoder[Series] {
    override def apply(a: Series): Json = Json.obj(
      "data" -> a.map(x => x.asJson).asJson,
      "$type" -> "Series".asJson,
      "field" -> Encoder[Field].apply(a.field)
    )
  }

  implicit val dec_series: Decoder[Series] = new Decoder[Series] {
    override def apply(c: HCursor): Result[Series] = {

      val field_opt = c.downField("field").as[Field]
      field_opt.map(field => {
        val builder = table_manager.incremental_series_builder(field)
        c.downField("data").values.foreach(_.foreach {
          case x if x.isNull => builder :+ None
          case x if x.isBoolean => builder :+ x.asBoolean
          case x if x.isNumber => x.asNumber.foreach {v =>
            field.field_type.tpe match {
              case VectorType.Real => builder :+ Some(v.toDouble)
              case VectorType.Int => builder :+ v.toInt
              case VectorType.Bool => builder :+ v.toInt.map(_ != 0)
              case VectorType.Char => builder :+ Some(v.toString)
            }
          }
          case x if x.isString => builder :+ x.asString
        })
        builder.toSeries
      })
    }
  }

  implicit val enc_table: Encoder[Table] = new Encoder[Table] {
    override def apply(a: Table): Json = Json.obj(
      "$type" -> "Table".asJson,
      "columns" -> a.column_iter.map(_.asJson).toSeq.asJson
    )
  }

  implicit val dec_table: Decoder[Table] = new Decoder[Table] {
    override def apply(c: HCursor): Result[Table] = {
      c.downField("columns").as[Seq[Series]].map(columns => {
        val schema = Schema(columns.map(x => x.field))
        new TableImpl(schema, columns)
      })
    }
  }

  def round_trip[A : PrettyPrinter](x : A)(implicit ev_enc: Encoder[A], ev_dec: Decoder[A]): Unit = {
    val x_json = x.asJson
    val x_back = x_json.as[A]
    println(pprint(x))
    println(x_json)
    x_back.map((x : A) => println(pprint(x)))
  }
}
