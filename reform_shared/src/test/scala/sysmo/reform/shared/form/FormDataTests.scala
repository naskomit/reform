package sysmo.reform.shared.form

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import sysmo.reform.shared.util.LabeledValue


class FormDataTests extends AnyFunSpec with Matchers {
  describe("ValueMap tests") {
    val vm1 = ValueMap.builder
      .value("foo", "bar")
      .value("name", "Peter")
      .record("sub1", _
        .value("name", "subPeter")
        .record("sub2", _
          .value("name", "subsubPeter")
        )
      )
      .build
    val p1 = ElementPath.str("sub1")
    val p2 = ElementPath.str("sub1/sub2")
    val p3 = ElementPath.str("sub1/sub2/name")
    it("check elements") {
      assert(
        vm1.get(p3) == SomeValue(LabeledValue("subsubPeter"))
      )
      assert(vm1.iterator.size == 4)
    }

    it("check subpaths") {
      assert(vm1.subpaths(p1).length == 2)
      assert(vm1.subpaths(p2).length == 1)
    }

    it("check remove") {
      assert(vm1.remove(p3).get(p3) == NoValue)
      assert(vm1.remove(p2).get(p3) != NoValue)
      assert(vm1.remove_all(vm1.subpaths(p2)).get(p3) == NoValue)
      assert(vm1.remove_all(vm1.subpaths(p2)).iterator.size == 3)
      assert(vm1.remove_all(vm1.subpaths(p1)).iterator.size == 2)
    }
  }
}

