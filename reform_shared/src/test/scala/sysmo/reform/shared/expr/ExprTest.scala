package sysmo.reform.shared.expr

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import scala.reflect.ClassTag

class ExprTest extends AnyFunSpec with Matchers {
  import sysmo.reform.shared.expr.{Expression => E}
  import Expression._
  describe("Expression evaluation") {
    val const_5 = Constant(5)
    val const_hello = Constant("Hello")
    val const_world = Constant("World")
    val ref_x = FieldRef("x")
    val ref_y = FieldRef("y")
    val ref_z = FieldRef("z")
    val ctx = Context(Map("x" -> 2, "y" -> 3, "s1" -> "Hello", "s2" -> "World"))

    it("evaluate constants") {
      assert(Expression.eval[Int](const_5, ctx) == Right(5))
      Expression.eval[String](const_5, ctx).to_error shouldBe a [IncorrectTypeError]
    }
    it("evaluate field reference") {
      assert(Expression.eval[Int](ref_x, ctx) == Right(2))
      Expression.eval[String](ref_z, ctx).to_error shouldBe a [NoSuchFieldError]
    }

    it("evaluate equals and not equals") {
      Expression.eval[Boolean](E("Hello") === E("Hello"), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E("Hello") === E("World"), ctx) shouldBe Right(false)
      Expression.eval[Boolean](E("Hello") !== E("Hello"), ctx) shouldBe Right(false)
      Expression.eval[Boolean](E("Hello") !== E("World"), ctx) shouldBe Right(true)
    }
    it("evaluate >, < ") {
      Expression.eval[Boolean](E(8) > E(6), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E(2) > E(4), ctx) shouldBe Right(false)
      Expression.eval[Boolean](E(3) >= E(3), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E(7) >= E(3), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E(2) < E(4), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E(6) < E(1), ctx) shouldBe Right(false)
      Expression.eval[Boolean](E(5) <= E(5), ctx) shouldBe Right(true)
      Expression.eval[Boolean](E(9) <= E(5), ctx) shouldBe Right(false)

    }


  }
}
