package sysmo.reform.shared.form

import sysmo.reform.shared.expr.Expression
import sysmo.reform.shared.gremlin.tplight
import sysmo.reform.shared.gremlin.tplight.gobject.{EdgeDef, EdgeObj, Property, PropertyDef, VertexDef}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalBuilder, GraphTraversalSource, Vertex}
import sysmo.reform.shared.util.INamed
import sysmo.reform.shared.{expr => E}

trait FieldId {
  def as_key: String = ""
}

case class NamedFieldId(id: String) extends FieldId {
  override def toString: String = id
  override def as_key: String = id
}

case class ArrayFieldId(id: java.util.UUID) extends FieldId {
  override def as_key: String = id.toString
}

object ArrayFieldId {
  import java.util.UUID
  def random(): ArrayFieldId = ArrayFieldId(UUID.randomUUID())
}

case object Up extends FieldId
case object Missing extends FieldId

case class ElementPath(segments: Seq[FieldId]) {
  def / (k: FieldId): ElementPath = k match {
    case Up => if (segments.nonEmpty) ElementPath(segments.dropRight(1)) else this
    case _ => ElementPath(segments :+ k)
  }

  def / (k: String): ElementPath = {
    val key_segments: Seq[String] = k.split("/")
    val new_segments: Seq[FieldId] = key_segments.foldLeft(segments)((acc, segment) => segment match {
      case ".." => if (acc.nonEmpty) acc.dropRight(1) else acc
      case x => acc :+ NamedFieldId(x)
    })
    ElementPath(new_segments)
  }

  def contains(other: ElementPath): Boolean = {
    if (segments.isEmpty)
      true
    else if ((segments.length > other.segments.length) || (segments.head != other.segments.head))
      false
    else ElementPath(segments.tail).contains(ElementPath(other.segments.tail))

  }

  override def toString: String = segments.mkString("/")
}

object PathMatch {
  def unapply(path: ElementPath): Option[Seq[String]] = {
    Some(path.segments.map(_.toString))
  }
}

object ElementPath {
  val Empty = ElementPath(Seq())
}

case class FormElementBackend[T <: FormElement.Def](
  vertex: Vertex,
  parent: Option[FormElement],
  fid: FieldId,
  ed: T
) extends GO.VertexObj {
  override type ED = T
}

trait FormElement {
//  type ED <: FormElement.Def
  val backend: FormElementBackend[_ <: FormElement.Def]
  def fid: FieldId = backend.fid match {
    case Missing => backend.get(_.fid).get
    case x => x
  }
  def name: String = fid match {
    case NamedFieldId(id) => id
  }
  def descr: String = backend.get(_.descr).getOrElse(name)

  def path: ElementPath = {
    backend.parent match {
      case Some(p) => p.path / fid
      case None => ElementPath(Seq(fid))
    }
  }

  def show(ctx: HandlerContext): E.Result[Boolean] = {
    val show_expr: Expression = backend.get(_.show_expr).get
    val res = E.as[Boolean](E.Expression.eval(show_expr, ctx))
    res
  }

}

object FormElement {
  trait Def extends VertexDef {
    trait Props extends PropertyDef {
      //    val name = GO.Property[String]("name")
      val fid = GO.Property[FieldId]("fid")
      val descr = GO.Property[String]("descr")
      val show_expr = GO.Property[E.Expression]("show_expr", Some(E.Expression(true)))
    }
    val props = new Props {}
  }

  trait Builder[+T <: FormElement] {
    protected val ed: FormElement.Def
    protected val graph: Graph
    protected val fid: FieldId
    lazy val vertex: Vertex = graph.add_vertex(ed.label, (ed.props.fid.name -> fid))

    def descr(v: String): this.type = {
      vertex.property(ed.props.descr.name, v)
      this
    }
    def show(expr: E.Expression): this.type = {
      vertex.property(ed.props.show_expr.name, expr)
      this
    }
    def build: T
  }

  def create_backend[DT <: FormElement.Def](v: Vertex, fid: FieldId, parent: Option[FormElement], dt: DT): FormElementBackend[DT] = {
    val fid1: FieldId = fid match {
      case Missing => v.value[FieldId](dt.props.fid.name).get
      case x => x
    }
    new FormElementBackend[DT](v, parent, fid1, dt)
  }

  def from_vertex(v: Vertex, fid: FieldId, parent: Option[FormElement]): FormElement = {
    v.label match {
      case StringEditor.Def.label => StringEditor(create_backend(v, fid, parent, StringEditor.Def))
      case FloatEditor.Def.label => FloatEditor(create_backend(v, fid, parent, FloatEditor.Def))
      case IntegerEditor.Def.label => IntegerEditor(create_backend(v, fid, parent, IntegerEditor.Def))
      case BooleanEditor.Def.label => BooleanEditor(create_backend(v, fid, parent, BooleanEditor.Def))
      case SelectEditor.Def.label => SelectEditor(create_backend(v, fid, parent, SelectEditor.Def))
      case FormGroup.Def.label => FormGroup(create_backend(v, fid, parent, FormGroup.Def))
      case GroupArray.Def.label => GroupArray(create_backend(v, fid, parent, GroupArray.Def))
    }
  }
}

trait FieldEditor extends FormElement {

}

object FieldEditor {
  trait Builder[+T <: FieldEditor] extends FormElement.Builder[T]

  class BuilderSource(graph: Graph, parent: Option[FormElement]) {
    def char(name: String): StringEditor.Builder = new StringEditor.Builder(graph, parent, name)
    def float(name: String): FloatEditor.Builder = new FloatEditor.Builder(graph, parent, name)
    def bool(name: String): BooleanEditor.Builder = new BooleanEditor.Builder(graph, parent, name)
    def int(name: String): IntegerEditor.Builder = new IntegerEditor.Builder(graph, parent, name)
    def select(name: String): SelectEditor.Builder = new SelectEditor.Builder(graph, parent, name)
  }}

case class StringEditor(backend: FormElementBackend[StringEditor.Def.type]) extends FieldEditor

object StringEditor {
  object Def extends FormElement.Def {
    val label = "StringEditor"
  }
  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FieldEditor.Builder[StringEditor] {
    val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    override def build: StringEditor =
      StringEditor(FormElement.create_backend(vertex, Missing, parent, ed))
  }
}

case class FloatEditor(backend: FormElementBackend[FloatEditor.Def.type]) extends FieldEditor

object FloatEditor {
  object Def extends FormElement.Def {
    val label: String = "FloatEditor"
  }
  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FieldEditor.Builder[FloatEditor] {
    val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    override def build: FloatEditor = {
      FloatEditor(FormElement.create_backend(vertex, Missing, parent, ed))
    }
  }
}

case class IntegerEditor(backend: FormElementBackend[IntegerEditor.Def.type]) extends FieldEditor

object IntegerEditor {
  object Def extends FormElement.Def {
    val label: String = "IntegerEditor"
  }

  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FieldEditor.Builder[IntegerEditor] {
    val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    override def build: IntegerEditor = {
      IntegerEditor(FormElement.create_backend(vertex, Missing, parent, ed))
    }
  }
}

case class BooleanEditor(backend: FormElementBackend[BooleanEditor.Def.type]) extends FieldEditor

object BooleanEditor {
  object Def extends FormElement.Def {
    val label: String = "BooleanEditor"
  }

  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FieldEditor.Builder[BooleanEditor] {
    val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    override def build: BooleanEditor = {
      BooleanEditor(FormElement.create_backend(vertex, Missing, parent, ed))
    }
  }
}

case class SelectEditor(backend: FormElementBackend[SelectEditor.Def.type]) extends FieldEditor {
  def multiple: Boolean = backend.get(_.multiple).getOrElse(false)
  def min: Option[Int] = backend.get(_.min)
  def max: Option[Int] = backend.get(_.max)
}

object SelectEditor {
  object Def extends FormElement.Def {
    trait Props extends super.Props {
      val multiple = GO.Property[Boolean]("multiple", Some(false))
      val min = GO.Property[Int]("min")
      val max = GO.Property[Int]("max")
    }
    override val props = new Props {}
    val label: String = "SelectEditor"
  }

  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FieldEditor.Builder[SelectEditor] {
    val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    def multiple(max: Option[Int] = None, min: Option[Int] = None): this.type = {
      vertex.property(Def.props.multiple.name, true)
      max match {
        case Some(x) => vertex.property(Def.props.max.name, x)
        case None => vertex.property(Def.props.max.name).remove()
      }
      min match {
        case Some(x) => vertex.property(Def.props.min.name, x)
        case None => vertex.property(Def.props.min.name).remove()
      }
      this
    }
    override def build: SelectEditor = {
      new SelectEditor(FormElement.create_backend(vertex, Missing, parent, ed))
    }
  }

}

case class FormGroup(backend: FormElementBackend[FormGroup.Def.type]) extends FormElement {
  def elements: Seq[FormElement] = {
    backend.g_this.outE(HasElementDef.label).build
      .toSeq.sortBy(e => HasElement(e).get(_.seq_num).getOrElse(0))
      .map(e => FormElement.from_vertex(e.in_vertex, Missing, Some(this)))
  }
}

case class HasElement(edge: Edge) extends EdgeObj {
  override type ED = HasElementDef.type
  override val ed: ED = HasElementDef
}

object HasElementDef extends EdgeDef {
  override val label: String = "has_element"
  trait Props extends PropertyDef {
    val seq_num = Property[Int]("seq_num")
  }
  val props = new Props {}
}

object FormGroup {
  object Def extends FormElement.Def {
    val label: String = "FormGroup"
  }

  class Builder(val graph: Graph, parent: Option[FormElement], val name: String) extends FormElement.Builder[FormGroup] {
    override protected val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def
    protected def add_element(element: FormElement): this.type = {
      val new_seq_num = vertex.edges(Direction.OUT, Seq(HasElementDef.label))
        .map(e => HasElement(e).get(_.seq_num).get).toSeq.sorted.lastOption.getOrElse(-1) + 1
      vertex.add_edge(HasElementDef.label, element.backend.vertex, (HasElementDef.props.seq_num.name -> new_seq_num))
      this
    }

    def group(name: String, f: Builder => Builder): this.type = {
      add_element(
        f(new FormGroup.Builder(graph, None, name)).build
      )
    }

    def field(f: FieldEditor.BuilderSource => FieldEditor.Builder[FieldEditor]): this.type = {
      add_element(
        f(new FieldEditor.BuilderSource(graph, None)).build
      )
    }

    def array(name: String, f: Builder => Builder): this.type = {
      val group = f(new FormGroup.Builder(graph, None, name + "###")).build
      val array = new GroupArray.Builder(graph, None, name).build
      array.backend.vertex.add_edge(HasPrototype.Def.label, group.backend.vertex)
      add_element(array)
    }

    override def build: FormGroup = {
      new FormGroup(FormElement.create_backend(vertex, Missing, parent, ed))
    }
  }

  def builder(graph: Graph, name: String): Builder =
    new Builder(graph, None, name)
  def builder(graph: Graph, parent: Option[FormElement], name: String): Builder =
    new Builder(graph, parent, name)
}

case class GroupArray(backend: FormElementBackend[GroupArray.Def.type]) extends FormElement {
  def group: FormGroup = {
    val group_vertex: Vertex = backend.g_this.out(HasPrototype.Def.label).build.nextOption().get
    FormElement.from_vertex(group_vertex, Missing, Some(this)).asInstanceOf[FormGroup]
  }
  def elements(data: ValueMap): Seq[FormGroup] = {
    val group_vertex: Vertex = backend.g_this.out(HasPrototype.Def.label).build.nextOption().get

    val element_fids: Seq[FieldId] = data.get(path) match {
      case LocalFieldIndex(ids) => ids
    }
    element_fids.map{id =>
      FormElement.from_vertex(group_vertex, id, Some(this)).asInstanceOf[FormGroup]
    }
  }
}

case class HasPrototype(edge: Edge) extends EdgeObj {
  override type ED = HasPrototype.Def.type
  override val ed: ED = HasPrototype.Def
}

object HasPrototype {
  object Def extends EdgeDef {
    override val label: String = "has_prototype"
    trait Props extends PropertyDef {
    }
    val props = new Props {}
  }
}

object GroupArray {
  object Def extends FormElement.Def {
    val label: String = "GroupArray"
    trait Props extends super.Props {
    }
    override val props = new Props {}
  }

  class Builder(val graph: Graph, parent: Option[FormElement], name: String) extends FormElement.Builder[GroupArray] {
    override protected val fid: FieldId = NamedFieldId(name)
    override protected val ed: Def.type = Def

    override def build: GroupArray = {
      new GroupArray(FormElement.create_backend(vertex, Missing, parent, ed))
    }

  }

  def builder(graph: Graph, parent: Option[FormElement], name: String): Builder =
    new Builder(graph, parent, name)
}