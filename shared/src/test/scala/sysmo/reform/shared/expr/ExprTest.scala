package sysmo.reform.shared.expr

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.data.Value

class ExprTest extends AnyFunSpec with Matchers {
  import Value.implicits._
  import E.implicits._
  import sysmo.reform.shared.util.MonadTesting.implicits._

  describe("Expression evaluation") {
    val ctx = Context.fromMap_Either(Map[String, Value](
      "x" -> 2, "y" -> 3, "x1" -> "x1", "x10" -> "x10",
      "s1" -> "Hello", "s2" -> "World")
    )

    type F[+T] = Either[Throwable, T]
    val ev = Evaluator[F]

    it("evaluate constants") {
      ev.eval(E(5), ctx).map { x =>
        assert(x == Value(5))
      }.detect_error()
    }
    it("evaluate field reference") {
      ev.eval(E.field("x"), ctx).map {x =>
        assert(x == Value(2))
      }.detect_error()

      ev.eval(E.field("z"), ctx).map {x =>
        assert(x == Value.empty)
      }.detect_error()
    }

    it("evaluate equals and not equals") {
      ev.eval(E("Hello") === E("Hello"), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E("Hello") === E("World"), ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval(E("Hello") !== E("Hello"), ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval(E("Hello") !== E("World"), ctx).map(_ shouldBe Value(true)).detect_error()
    }

    it("evaluate >, < ") {
      ev.eval(E(8) > E(6), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E(2) > E(4), ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval(E(3) >= E(3), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E(7) >= E(3), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E(2) < E(4), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E(6) < E(1), ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval(E(5) <= E(5), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval(E(9) <= E(5), ctx).map(_ shouldBe Value(false)).detect_error()
    }

    it ("evaluate and") {
      ev.eval((E(5) > E(3)) && (E(6) < E(10)), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval((E(5) < E(3)) && (E(6) < E(10)), ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval((E(5) > E(3)) && (E(6) > E(10)), ctx).map(_ shouldBe Value(false)).detect_error()
    }

    it ("evaluate or") {
      ev.eval((E(5) > E(3)) || (E(6) < E(10)), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval((E(5) < E(3)) || (E(6) < E(10)), ctx).map(_ shouldBe Value(true)).detect_error()
      ev.eval((E(5) < E(3)) || (E(6) > E(10)), ctx).map(_ shouldBe Value(false)).detect_error()
    }

    it ("evaluate not") {
      ev.eval((E(5) >= E(3)).not, ctx).map(_ shouldBe Value(false)).detect_error()
      ev.eval((E(5) < E(3)).not, ctx).map(_ shouldBe Value(true)).detect_error()
    }
//    test1()
//    it("evaluate within/without") {
//      ev.eval(E.field("x1") within Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(true)
//      ev.eval(E.field("x10") within Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(false)
//      ev.eval(E.field("x1") without Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(false)
//      ev.eval(E.field("x10") without  Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(true)
//    }
  }

//  def test1(): Unit = {
//    trait Show {
//      def show: String
//    }
//    class X1(var i: Seq[Int] = Seq()) {
//      def inc(): Unit = {
//        i = i :+ 1
//        println(s"Incremented i to $i")
//      }
//      def build = new Show {
//        val v = i
//        override def show: String = s"Value $v (i = $i)"
//      }
//    }
//
//    val x1 = new X1()
//    x1.inc()
//    x1.inc()
//    val y: Show = x1.build
//    println(y.show)
//    x1.inc()
//    x1.inc()
//    println(y.show)
//  }
}
