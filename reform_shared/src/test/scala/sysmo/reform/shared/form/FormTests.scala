package sysmo.reform.shared.form

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

class FormTests extends AnyFunSpec with Matchers {
  describe("Element path tests") {
    val p1 = ElementPath.Empty / "x/y/z"
    val p2 = ElementPath.Empty / "x/y/z/t"
    val p3 = ElementPath.Empty / "x/y/z/../t2"
    println(p1)
    it("path formation") {
      assert(p1 / "t" == p2)
      assert(p1 / "zz" != p2)
      assert(p1 / "t2" != p3)
    }
    it("path contains") {
      assert(!p1.contains(ElementPath.Empty))
      assert(ElementPath.Empty.contains(p1))
      assert(!p2.contains(p1))
      assert(p1.contains(p2))
    }
  }
}
