package sysmo.reform.shared.form.build

import sysmo.reform.shared.gremlin.tplight.{gobject => GO}
import sysmo.reform.shared.gremlin.{tplight => TP}


trait FormElement extends GO.VertexObj {
  def symbol: String
}

object FormElement {
  def from_vertex(v: TP.Vertex): Option[FormElement] = v.label match {
    case FieldGroup.Def.label => Some(new FieldGroup(v))
    case GroupUnion.Def.label => Some(new GroupUnion(v))
    case GroupArray.Def.label => Some(new GroupArray(v))
    case Reference.Def.label => Some(new Reference(v))
    case _ => AtomicField.from_vertex(v)
  }
}

trait FormElementCompanion[U] {
  /** The form element type to which this is companion */
  type FET = U
  trait IBuilder {
    protected val graph: TP.Graph
    lazy val vertex: TP.Vertex = graph.add_vertex(Def.label)
    def set_prop(pf: Def.props.type => GO.Property[_], value: Any): this.type = {
      vertex.property(pf(Def.props).name, value)
      this
    }

    def build: FET
  }

  trait IDef extends GO.VertexDef {
    trait Props extends GO.PropertyDef {
    }
  }

  val Def: IDef
  type Builder <: IBuilder
}

trait AbstractGroup extends FormElement


trait FormRelation extends GO.EdgeObj {

}

trait FormRelationCompanion[U] {
  /** The form relation type to which this is companion */
  type FRT = U
  trait IBuilder {
    protected val graph: TP.Graph
    val edge: TP.Edge
    def set_prop(pf: Def.props.type => GO.Property[_], value: Any): this.type = {
      edge.property(pf(Def.props).name, value)
      this
    }
    def build: FRT
  }

  trait IDef extends GO.EdgeDef {
    trait Props extends GO.PropertyDef {
    }
  }

  val Def: IDef
  type Builder <: IBuilder

}