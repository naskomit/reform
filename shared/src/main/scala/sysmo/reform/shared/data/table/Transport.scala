package sysmo.reform.shared.data.table

import sysmo.reform.shared.util.CirceTransport

object Transport extends CirceTransport {
  import io.circe.generic.semiauto._
  import io.circe.syntax._

//  implicit val enc_value: Encoder[Value[_]] = new Encoder[Value[_]] {
//    override def apply(a: Value[_]): Json = {
//      a match {
//        case _ if a.is_na => Json.Null
//        case RealValue(x) => Json.fromDoubleOrNull(x.get)
//        case IntValue(x) => Json.fromInt(x.get)
//        case BoolValue(x) => Json.fromBoolean(x.get)
//        case CharValue(x) => Json.fromString(x.get)
//        case x => throw new IllegalStateException(f"Cannot encode value $x")
//      }
//    }
//  }

  implicit val enc_field_type: Encoder[FieldType] = new Encoder[FieldType] {
    override def apply(a: FieldType): Json = Json.obj(
      "tpe" -> a.tpe.toString.asJson,
      "nullable" -> a.nullable.asJson,
      "ext_class" -> a.ext_class.asJson,
      "categories" -> a.categories.asJson,
      "metadata" -> a.metadata.asJson
    )
  }

//  sealed trait ExtClass
//  case object Same extends ExtClass
//  case object Categorical extends ExtClass
//  case object Date extends ExtClass
//  case object DateTime extends ExtClass
  implicit val codec_ExtClass: Codec[ExtClass] = deriveCodec[ExtClass]
//  implicit val codec_Same: Codec[Same] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]
//  implicit val codec_: Codec[] = deriveCodec[]

  implicit val dec_field_type: Decoder[FieldType] = new Decoder[FieldType] {
    override def apply(c: HCursor): Decoder.Result[FieldType] = for {
      tpe <- c.downField("tpe").as[String]
      nullable <- c.downField("nullable").as[Boolean]
      ext_class <- c.downField("ext_class").as[ExtClass]
      categories <- c.downField("categories").as[Seq[String]]
      metadata <- c.downField("metadata").as[Map[String, String]]

    } yield FieldType(VectorType.withName(tpe), nullable, ext_class, categories, metadata)
  }

  implicit val codec_field: Codec[Field] = deriveCodec[Field]

  implicit val enc_series: Encoder[Series] = new Encoder[Series] {
    override def apply(a: Series): Json = Json.obj(
      "data" -> a.map {
        case x if x.is_na => Json.Null
        case RealValue(x) => Json.fromDoubleOrNull(x.get)
        case IntValue(x) => Json.fromInt(x.get)
        case BoolValue(x) => Json.fromBoolean(x.get)
        case CharValue(x) => Json.fromString(x.get)
        case DateValue(x) => Json.fromDoubleOrNull(x.get)
        case CategoricalValue(x, _) => Json.fromInt(x.get)
        case x => throw new IllegalStateException(f"Cannot encode value $x")
      }.asJson,
      "$type" -> "Series".asJson,
      "field" -> Encoder[Field].apply(a.field)
    )
  }

  implicit val dec_series: Decoder[Series] = new Decoder[Series] {
    override def apply(c: HCursor): Decoder.Result[Series] = {

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
          case x => throw new IllegalStateException(f"Cannot decode value $x")
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
    override def apply(c: HCursor): Decoder.Result[Table] = {
      c.downField("columns").as[Seq[Series]].map(columns => {
        val schema = Schema(columns.map(x => x.field))
        new TableImpl(schema, columns)
      })
    }
  }

  import sysmo.reform.shared.util.pprint
  def round_trip[A : pprint.PrettyPrinter](x : A)(implicit ev_enc: Encoder[A], ev_dec: Decoder[A]): Unit = {
    val x_json = x.asJson
    val x_back = x_json.as[A]
    pprint.pprint(x)
    println(x_json)
    x_back.map((x : A) => pprint.pprint(x))
  }
}
