package sysmo.reform.shared.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

class PropertyHolderTest extends AnyFunSpec with Matchers {
  object FormRenderingProps {
    val background = Property[Boolean]("background", false)
    val level = Property[Int]("level", 0)
  }

  class FormRenderingOptions extends PropertyHolder[FormRenderingOptions] {
    override type Props = FormRenderingProps.type
    override val props = FormRenderingProps
    def fork: Concrete = new FormRenderingOptions
  }


  describe("Test PropertyHolder") {
    val opts1 = new FormRenderingOptions
    it("default values") {
      opts1.get(_.background) shouldBe false
      opts1.get(_.level) shouldBe 0
    }
    val opts2 = opts1.update(_.background := true, _.level := 4)
    it("update values") {
      opts2.get(_.background) shouldBe true
      opts2.get(_.level) shouldBe 4
    }
    val opts3 = opts2.update(_.background.clear)
    it("clear value 1") {
      opts3.get(_.background) shouldBe false
      opts3.get(_.level) shouldBe 4
    }
    val opts4 = opts3.update(_.level.clear)
    it("clear value 2") {
      opts4.get(_.background) shouldBe false
      opts4.get(_.level) shouldBe 0
    }

    val update5 = PropertyHolder.update(FormRenderingProps)(
      _.background := true, _.level := 6
    )
    val opts5 = opts1.update(update5)
    it("using update object") {
      opts5.get(_.background) shouldBe true
      opts5.get(_.level) shouldBe 6
    }
  }

}
