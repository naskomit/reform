package sysmo.reform.shared.expr

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import sysmo.reform.shared.data

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.data.Value

class ExprTest extends AnyFunSpec with Matchers {
  import Value.implicits._
  describe("Expression evaluation") {
    val ctx = Context.fromMap_Either[Value](Map[String, Value](
      "x" -> 2, "y" -> 3, "x1" -> "x1", "x10" -> "x10",
      "s1" -> "Hello", "s2" -> "World")
    )



    type F[+T] = Either[Throwable, T]
    val ev = Evaluator[F]
    import Value._

    it("evaluate constants") {
      println(s"evaluate constants")
      ev.eval(E(5), ctx) map {x =>
        println(s"Result is $x")
        assert(x == Value(6))
      }
    }
//    it("evaluate field reference") {
//      assert(E.eval(E.field("x"), ctx) == Right(2))
//      E.eval(E.field("z"), ctx).to_error shouldBe a [NoSuchFieldError]
//    }
//
//    it("evaluate equals and not equals") {
//      E.eval(E("Hello") === E("Hello"), ctx) shouldBe Right(true)
//      E.eval(E("Hello") === E("World"), ctx) shouldBe Right(false)
//      E.eval(E("Hello") !== E("Hello"), ctx) shouldBe Right(false)
//      E.eval(E("Hello") !== E("World"), ctx) shouldBe Right(true)
//    }
//
//    it("evaluate >, < ") {
//      E.eval(E(8) > E(6), ctx) shouldBe Right(true)
//      E.eval(E(2) > E(4), ctx) shouldBe Right(false)
//      E.eval(E(3) >= E(3), ctx) shouldBe Right(true)
//      E.eval(E(7) >= E(3), ctx) shouldBe Right(true)
//      E.eval(E(2) < E(4), ctx) shouldBe Right(true)
//      E.eval(E(6) < E(1), ctx) shouldBe Right(false)
//      E.eval(E(5) <= E(5), ctx) shouldBe Right(true)
//      E.eval(E(9) <= E(5), ctx) shouldBe Right(false)
//    }
//
//    it ("evaluate and") {
//      E.eval((E(5) > E(3)) && (E(6) < E(10)), ctx) shouldBe Right(true)
//      E.eval((E(5) < E(3)) && (E(6) < E(10)), ctx) shouldBe Right(false)
//      E.eval((E(5) > E(3)) && (E(6) > E(10)), ctx) shouldBe Right(false)
//    }
//
//    it ("evaluate or") {
//      E.eval((E(5) > E(3)) || (E(6) < E(10)), ctx) shouldBe Right(true)
//      E.eval((E(5) < E(3)) || (E(6) < E(10)), ctx) shouldBe Right(true)
//      E.eval((E(5) < E(3)) || (E(6) > E(10)), ctx) shouldBe Right(false)
//    }
//
//    it ("evaluate not") {
//      E.eval((E(5) >= E(3)).not, ctx) shouldBe Right(false)
//      E.eval((E(5) < E(3)).not, ctx) shouldBe Right(true)
//    }
//
//    it("evaluate within/without") {
//      E.eval(E.field("x1") within Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(true)
//      E.eval(E.field("x10") within Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(false)
//      E.eval(E.field("x1") without Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(false)
//      E.eval(E.field("x10") without  Seq(E("x1"), E("x2"), E("x3")), ctx) shouldBe Right(true)
//    }
  }
}
