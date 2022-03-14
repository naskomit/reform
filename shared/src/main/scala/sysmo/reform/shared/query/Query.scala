package sysmo.reform.shared.query

import scala.util.{Try, Success, Failure}

/** # Expression */
sealed trait Expression

case class ColumnRef(id: String, alias: Option[String] = None, table: Option[String] = None) extends Expression

case class Val(v: Any) extends Expression
//case class RealValue(v: Double) extends Expression
//case class StringValue(v: String) extends Expression
//case class BoolValue(v: Boolean) extends Expression

sealed trait PredicateExpression extends Expression

case class LogicalAnd(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalOr(expr_list: PredicateExpression*)
  extends PredicateExpression

case class LogicalNot(expr: PredicateExpression)
  extends PredicateExpression

trait PredicateOp extends Enumeration

object NumericalPredicateOp extends PredicateOp {
  type NumericalPredicateOp = Value
  val Equal, NotEqual, >, >=, <, <= = Value
}

case class NumericalPredicate(op: NumericalPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object StringPredicateOp extends PredicateOp {
  type StringPredicateOp = Value
  val Equal, NotEqual, StartingWith, NonStartingWith, EndingWith, NotEndingWith, Containing, NotContaining = Value
}

case class StringPredicate(op: StringPredicateOp.Value, arg1: Expression, arg2: Expression)
  extends PredicateExpression

object ContainmentPredicateOp extends PredicateOp {
  val Within, Without = Value
}

case class ContainmentPredicate(op: ContainmentPredicateOp.Value, arg1: ColumnRef, arg2: Seq[Val])
  extends PredicateExpression

/** # Filter */
case class QueryFilter(expr: PredicateExpression)

/** # Sort */
case class ColumnSort(col: ColumnRef, ascending: Boolean)

case class QuerySort(column_sorts: ColumnSort*)

/** # Source */
sealed trait QuerySource
case class SingleTable(id: String, alias: Option[String] = None, schema: Option[String] = None) extends QuerySource

/** # Range */
case class QueryRange(start: Int, length: Int)


/** # Query */
sealed trait Query

case class BasicQuery(
   source: QuerySource,
   columns: Option[Seq[ColumnRef]] = None,
   filter: Option[QueryFilter] = None,
   sort: Option[QuerySort] = None,
   range: Option[QueryRange] = None
) extends Query


//object ReadersWriters {
//import upickle.default.{macroRW, read, readwriter, writeJs, ReadWriter => RW}
//import ujson.Value

//  implicit val rwColumnRef: RW[ColumnRef] = macroRW
//  implicit val rwValue: RW[Val] = readwriter[Value].bimap[Val](
//    x => x.v match {
//      case y: Int => y
//      case y: Double => y
//      case y: Boolean => y
//      case y: String => y
//      case y => throw new IllegalArgumentException(f"Cannot serialize value $y")
//    },
//
//    x => x.value match {
//      case y: Int => Val(y)
//      case y: Double => Val(y)
//      case y: Boolean => Val(y)
//      case y: String => Val(y)
//      case y => throw new IllegalArgumentException(f"Cannot deserialize value $y")
//    }
//  )
////  implicit val rwRealValue: RW[RealValue] = macroRW
////  implicit val rwStringValue: RW[StringValue] = macroRW
////  implicit val rwBoolValue: RW[BoolValue] = macroRW
//  implicit val rwLogicalAnd: RW[LogicalAnd] = macroRW
//  implicit val rwLogicalOr: RW[LogicalOr] = macroRW
//  implicit val rwLogicalNot: RW[LogicalNot] = macroRW
//
//  implicit val rwNumericalPredicateOp: RW[NumericalPredicateOp.Value] =
//    readwriter[String].bimap[NumericalPredicateOp.Value](_.toString, NumericalPredicateOp.withName)
//  implicit val rwNumericalPredicate: RW[NumericalPredicate] = macroRW
//  implicit val rwStringPredicateOp: RW[StringPredicateOp.Value] =
//    readwriter[String].bimap[StringPredicateOp.Value](_.toString, StringPredicateOp.withName)
//  implicit val rwStringPredicate: RW[StringPredicate] = macroRW
//  implicit val rwContainmentPredicateOp: RW[ContainmentPredicateOp.Value] =
//    readwriter[String].bimap[ContainmentPredicateOp.Value](_.toString, ContainmentPredicateOp.withName)
//  implicit val rwContainmentPredicate: RW[ContainmentPredicate] = macroRW
//
//  implicit val rwPredicateExpression: RW[PredicateExpression] = RW.merge(
//    macroRW[NumericalPredicate],  macroRW[StringPredicate],  macroRW[ContainmentPredicate],
//    macroRW[LogicalAnd], macroRW[LogicalOr], macroRW[LogicalNot]
//  )
//
//  implicit val rwExpression: RW[Expression] = readwriter[Value].bimap[Expression](
//    {
//      case x: PredicateExpression => writeJs(x)
//      case x: ColumnRef => writeJs(x) //{val obj = ; obj("$type") = "ColumnRef"; obj }
//      case x: Val => writeJs(x)
//      case x => throw new IllegalArgumentException(f"Cannot serialize expression $x")
//    },
//    x => {
//      Try {
//        read[PredicateExpression](x)
//      } orElse Try {
//        read[ColumnRef](x)
//      } orElse Try {
//        read[Val](x)
//      } match {
//        case Success(v) => v.asInstanceOf[Expression]
//        case Failure(e) => throw new IllegalArgumentException(f"Cannot parse expression $x")
//      }
//
//    }
//  )
//
//  implicit val rwQueryFilter: RW[QueryFilter] = macroRW
//  implicit val rwColumnSort: RW[ColumnSort] = macroRW
//  implicit val rwQuerySort: RW[QuerySort] = macroRW
//  implicit val rwQuerySource: RW[QuerySource] = RW.merge(
//    macroRW[SingleTable]
//  )
//  implicit val rwSingleTable: RW[SingleTable] = macroRW
//  implicit val rwQueryRange: RW[QueryRange] = macroRW
//  implicit val rwQuery: RW[Query] = RW.merge(
//    macroRW[BasicQuery]
//  )
//  implicit val rwBasicQuery: RW[BasicQuery] = macroRW
//}

object TransportCirce {
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._
  import cats.implicits._
  import io.circe.generic.extras.Configuration
  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("$type")

  /** ## Query Expression */
  implicit val codec_ColumnRef: Codec[ColumnRef] = deriveCodec[ColumnRef]

  implicit val enc_Value: Encoder[Val] = Encoder.instance {
    case Val(y: Int) => y.asJson
    case Val(y: Double) => y.asJson
    case Val(y: Boolean) => y.asJson
    case Val(y: String) => y.asJson
    case Val(y) => throw new IllegalStateException(f"Cannot handle value $y")
  }

  implicit val dec_Value: Decoder[Val] = Decoder.instance (x => {
    x.as[Int].orElse(x.as[Double]).orElse(x.as[Boolean]).orElse(x.as[String]).map(Val)
  })

  implicit val codec_LogicalAnd: Codec[LogicalAnd] = deriveCodec
  implicit val codec_LogicalOr: Codec[LogicalOr] = deriveCodec
  implicit val codec_LogicalNot: Codec[LogicalNot] = deriveCodec

  implicit val enc_NumericalPredicateOp: Encoder[NumericalPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_NumericalPredicateOp: Decoder[NumericalPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(NumericalPredicateOp.withName)

  implicit val codec_NumericalPredicate: Codec[NumericalPredicate] = deriveCodec[NumericalPredicate]

  implicit val enc_StringPredicateOp: Encoder[StringPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_StringPredicateOp: Decoder[StringPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(StringPredicateOp.withName)

  implicit val codec_StringPredicate: Codec[StringPredicate] = deriveCodec[StringPredicate]

  implicit val enc_ContainmentPredicateOp: Encoder[ContainmentPredicateOp.Value] =
    Encoder.instance(x => x.toString.asJson)

  implicit val dec_ContainmentPredicateOp: Decoder[ContainmentPredicateOp.Value] =
    (x : HCursor) => x.as[String].map(ContainmentPredicateOp.withName)

  implicit val codec_ContainmentPredicate: Codec[ContainmentPredicate] = deriveCodec[ContainmentPredicate]

  implicit val codec_PredicateExpression: Codec[PredicateExpression] = deriveCodec[PredicateExpression]

  implicit val codec_Expression: Codec[Expression] = deriveCodec[Expression]


//  implicit val enc_PredicateExpression: Encoder[PredicateExpression] = Encoder.instance {
//    case x: NumericalPredicate => x.asJson
//    case x: StringPredicate =>  x.asJson
//    case x: ContainmentPredicate =>  x.asJson
//    case x: LogicalAnd =>  x.asJson
//    case x: LogicalOr =>  x.asJson
//    case x: LogicalNot =>  x.asJson
//  }

//  implicit val dec_PredicateExpression: Decoder[PredicateExpression] = List[Decoder[PredicateExpression]](
//    Decoder[NumericalPredicate].widen,
//    Decoder[StringPredicate].widen,
//    Decoder[ContainmentPredicate].widen,
//    Decoder[LogicalAnd].widen,
//    Decoder[LogicalOr].widen,
//    Decoder[LogicalNot].widen,
//  ).reduceLeft(_ or _)
//
//  implicit val enc_Expression: Encoder[Expression] = Encoder.instance {
//    case x: PredicateExpression => x.asJson
//    case x: ColumnRef => x.asJson
//    case x: Val => x.asJson
//  }
//
//  implicit val dec_Expression: Decoder[Expression] = List[Decoder[Expression]](
//    Decoder[PredicateExpression].widen,
//    Decoder[ColumnRef].widen,
//    Decoder[Val].widen
//  ).reduceLeft(_ or _)

  /** ## Query  Filter */
  implicit val codec_QueryFilter: Codec[QueryFilter] = deriveCodec

  /** ## Query Sort */
  implicit val codec_ColumnSort: Codec[ColumnSort] = deriveCodec
  implicit val codec_QuerySort: Codec[QuerySort] = deriveCodec

  /** ## Query Source */
  implicit val codec_SingleTable: Codec[SingleTable] = deriveCodec
  implicit val codec_QuerySource: Codec[QuerySource] = deriveCodec
//  implicit val enc_QuerySource: Encoder[QuerySource] = Encoder.instance {
//    case x: SingleTable => x.asJson
//  }
//  implicit val dec_QuerySource: Decoder[QuerySource] = List[Decoder[QuerySource]](
//    Decoder[SingleTable].widen
//  ).reduceLeft(_ or _)

  /** ## Query Range */
  implicit val codec_QueryRange: Codec[QueryRange] = deriveCodec
  /** ## Query */
  implicit val codec_BasicQuery: Codec[BasicQuery] = deriveCodec
  implicit val codec_Query: Codec[Query] = deriveCodec
//  implicit val enc_Query: Encoder[Query] = Encoder.instance {
//    case x: BasicQuery => x.asJson
//  }
//  implicit val dec_Query: Decoder[Query] = List[Decoder[Query]](
//    Decoder[BasicQuery].widen
//  ).reduceLeft(_ or _)
}

//object ReadersWritersCirce2 {
//  import io.circe._
//  import io.circe.generic.semiauto._
//  import io.circe.syntax._
//  import cats.implicits._
//  import shapeless.{Lazy, Generic, HNil, ::, Coproduct, CNil, Witness, :+:, Inl, Inr, LabelledGeneric}
//  import shapeless.labelled.{FieldType, field}
//  import io.circe.generic.extras.Configuration
//
//  implicit val genDevConfig: Configuration =
//    Configuration.default.withDiscriminator("$type")
//
//
//  implicit def encoderValueClass[T <: AnyVal, V](implicit
//      g: Lazy[Generic.Aux[T, V :: HNil]],
//      e: Encoder[V]
//    ): Encoder[T] = Encoder.instance { value =>
//    e(g.value.to(value).head)
//  }
//
//  implicit def decoderValueClass[T <: AnyVal, V](implicit
//     g: Lazy[Generic.Aux[T, V :: HNil]],
//     d: Decoder[V]
//    ): Decoder[T] = Decoder.instance { cursor =>
//    d(cursor).map { value =>
//      g.value.from(value :: HNil)
//    }
//  }
//
//  trait IsEnum[C <: Coproduct] {
//    def to(c: C): String
//    def from(s: String): Option[C]
//  }
//
//  object IsEnum {
//    implicit val cnilIsEnum: IsEnum[CNil] = new IsEnum[CNil] {
//      def to(c: CNil): String = sys.error("Impossible")
//      def from(s: String): Option[CNil] = None
//    }
//
//    implicit def cconsIsEnum[K <: Symbol, H <: Product, T <: Coproduct](implicit
//      witK: Witness.Aux[K],
//      witH: Witness.Aux[H],
//      gen: Generic.Aux[H, HNil],
//      tie: IsEnum[T]
//     ): IsEnum[FieldType[K, H] :+: T] = new IsEnum[FieldType[K, H] :+: T] {
//      def to(c: FieldType[K, H] :+: T): String = c match {
//        case Inl(h) => witK.value.name
//        case Inr(t) => tie.to(t)
//      }
//
//      def from(s: String): Option[FieldType[K, H] :+: T] =
//        if (s == witK.value.name) Some(Inl(field[K](witH.value)))
//        else tie.from(s).map(Inr(_))
//    }
//  }
//
//  implicit def encodeEnum[A, C <: Coproduct](implicit
//   gen: LabelledGeneric.Aux[A, C],
//   rie: IsEnum[C]
//  ): Encoder[A] = Encoder[String].contramap[A](a => rie.to(gen.to(a)))
//
//  implicit def decodeEnum[A, C <: Coproduct](implicit
//   gen: LabelledGeneric.Aux[A, C],
//   rie: IsEnum[C]
//  ): Decoder[A] = Decoder[String].emap { s =>
//    rie.from(s).map(gen.from).toRight("enum")
//  }

////  implicit val enc_Query: Encoder[Query] = deriveEncoder[Query]
////  implicit val dec_Query: Decoder[Query] = deriveDecoder[Query]
//
//}