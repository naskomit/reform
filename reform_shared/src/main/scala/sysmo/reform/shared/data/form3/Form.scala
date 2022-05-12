package sysmo.reform.shared.data.form3

import sysmo.reform.shared.util.INamed

import java.util.UUID


case class Form(name: String, label: Option[String], id: String, elements: Seq[FormElement]) extends INamed
case class ElementPath(segments: Seq[String]) {
  def :+ (k: String): ElementPath = ElementPath(segments :+ k)
  override def toString: String = segments.mkString("/")
}
object ElementPath {
  val Empty = ElementPath(Seq())
}

/** == Form Element == */
sealed trait FormElement extends INamed {
  val path: ElementPath
}


object FormElement {
  trait Builder[+T] {
    protected val name: String
    protected var _label: Option[String] = None
    protected var parent_path: ElementPath
    protected def get_path: ElementPath = ElementPath(parent_path.segments :+ name)
    def label(v: String): this.type = {
      _label = Some(v)
      this
    }
    def build: T
  }
}

/** == Field Editor == */
trait FieldEditor extends FormElement
object FieldEditor {
  trait Builder[+T] extends FormElement.Builder[T]
  type BuilderBase = Builder[FieldEditor]
  class BuilderSource(parent_path: ElementPath) {
    def char(name: String): StringEditor.Builder = new StringEditor.Builder(name, parent_path)
    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(name, parent_path)
    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(name, parent_path)
    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(name, parent_path)
    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(name, parent_path)
  }
}

case class StringEditor(name: String, label: Option[String], path: ElementPath) extends FieldEditor
object StringEditor {
  class Builder(val name: String, var parent_path: ElementPath) extends FieldEditor.Builder[StringEditor] {
    def build  = StringEditor(name, _label, get_path)
  }
}

case class IntegerEditor(name: String, label: Option[String], path: ElementPath) extends FieldEditor
object IntegerEditor {
  class Builder(val name: String, var parent_path: ElementPath) extends FieldEditor.Builder[IntegerEditor] {
    def build  = IntegerEditor(name, _label, get_path)
  }
}

case class FloatEditor(name: String, label: Option[String], path: ElementPath) extends FieldEditor
object FloatEditor {
  class Builder(val name: String, var parent_path: ElementPath) extends FieldEditor.Builder[FloatEditor] {
    def build  = FloatEditor(name, _label, get_path)
  }
}

case class BooleanEditor(name: String, label: Option[String], path: ElementPath) extends FieldEditor
object BooleanEditor {
  class Builder(val name: String, var parent_path: ElementPath) extends FieldEditor.Builder[BooleanEditor] {
    def build  = BooleanEditor(name, _label, get_path)
  }
}

case class SelectEditor(name: String, label: Option[String], path: ElementPath) extends FieldEditor
object SelectEditor {
  class Builder(val name: String, var parent_path: ElementPath) extends FieldEditor.Builder[SelectEditor] {
    def build  = SelectEditor(name, _label, get_path)
  }
}

/** == Form Group == */
trait FormGroup extends FormElement
object FormGroup {
  trait Builder[+T] extends FormElement.Builder[T]
  type BuilderBase = Builder[FormGroup]
  class BuilderSource(parent_path: ElementPath) {
    def fieldset(name: String): FieldSet.Builder = new FieldSet.Builder(name, parent_path)
    def section(name: String): Section.Builder = new Section.Builder(name, parent_path)
  }

}

case class FieldSet(name: String, label: Option[String], path: ElementPath, fields: Seq[FieldEditor]) extends FormGroup
object FieldSet {
  class Builder(val name: String, var parent_path: ElementPath) extends FormGroup.Builder[FieldSet] {
    protected var fields: Seq[FieldEditor] = Seq()
    def field(f: FieldEditor.BuilderSource => FieldEditor.BuilderBase): this.type = {
      fields = fields :+ f(new FieldEditor.BuilderSource(get_path)).build
      this
    }
    def build  = FieldSet(name, _label, get_path, fields)
  }
}

case class Section(name: String, label: Option[String], path: ElementPath, elements: Seq[FormElement]) extends FormGroup
object Section {
  class Builder(val name: String, var parent_path: ElementPath) extends FormGroup.Builder[Section] {
    protected var elements: Seq[FormElement] = Seq()
    def build  = Section(name, _label, get_path, elements)
  }
}
//
///** == Form Array == */
//trait FormArray extends FormElement
//case class FieldArray(name: String, label: Option[String], fields: Seq[FieldEditor]) extends FormArray
//object FormArray {
//  class Builder(val name: String) extends FormElement.Builder[FieldArray] {
//    protected var fields: Seq[FieldEditor] = Seq()
//    def build  = FieldArray(name, _label, fields)
//  }
//
//  def builder(name: String): Builder = new Builder(name)
//}
//
//case class GroupArray(name: String, label: Option[String], elements: Seq[FormGroup]) extends FormArray
//object GroupArray {
//  class Builder(val name: String) extends FormElement.Builder[GroupArray] {
//    protected var elements: Seq[FormGroup] = Seq()
//    def build  = GroupArray(name, _label, elements)
//  }
//
//  def builder(name: String): Builder = new Builder(name)
//}

object Form {
  class Builder(name: String, id: String) {
    protected var _label: Option[String] = None
    protected var elements: Seq[FormElement] = Seq()
    def label(v: String): this.type = {
      _label = Some(v)
      this
    }
    def field(f: FieldEditor.BuilderSource => FieldEditor.BuilderBase): this.type = {
      elements = elements :+ f(new FieldEditor.BuilderSource(ElementPath.Empty)).build
      this
    }

    def group(f: FormGroup.BuilderSource => FormGroup.BuilderBase): this.type = {
      elements = elements :+ f(new FormGroup.BuilderSource(ElementPath.Empty)).build
      this
    }

//    def fieldset(element: FieldSet): this.type = {
//      elements = elements :+ element
//      this
//    }
//
//    def array(element: FormArray): this.type = {
//      elements = elements :+ element
//      this
//    }

    def build: Form = Form(name, _label, id, elements)
  }

  // UUID.randomUUID().toString
  def builder(name: String): Builder = new Builder(name, name)
}
