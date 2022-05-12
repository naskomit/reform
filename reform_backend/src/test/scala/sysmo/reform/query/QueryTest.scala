package sysmo.reform.query

import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.parser.parse
import sysmo.reform.shared.util.pprint
import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.{query => Q}

import Q.Transport._

object QueryTest {
  def round_trip[A](x : A)(implicit ev_enc: Encoder[A], ev_dec: Decoder[A]): Unit = {
    val x_json = x.asJson.toString
    val x_back: Either[_, A] = parse(x_json).flatMap(_.as[A])
    println(x)
    println(x_json)
    x_back.foreach(println(_))
    x_back.foreach(res => println(res == x))
  }

  def test_serialization(): Unit = {
    val q = Q.BasicQuery(
      Q.SingleTable("PatientRecord", None, None),
      None,
      Some(Q.QueryFilter(E.LogicalOr(
        E.NumericalPredicate(E.NumericalPredicateOp.<, E.ColumnRef("age"), E.Val(35.0)),
        E.StringPredicate(E.StringPredicateOp.Equal, E.ColumnRef("gender"), E.Val("жена"))
      ))),
      Some(Q.QuerySort(Q.ColumnSort(E.ColumnRef("age"), false))),
      Some(Q.QueryRange(0, 100))
    )

//    round_trip(
//      Q.QueryFilter(Q.LogicalOr(
//        Q.NumericalPredicate(Q.NumericalPredicateOp.<, Q.ColumnRef("age"), Q.Val(35.0)),
//        Q.StringPredicate(Q.StringPredicateOp.Equal, Q.ColumnRef("gender"), Q.Val("жена"))
//      ))
//    )
    round_trip(q)

  }

}
