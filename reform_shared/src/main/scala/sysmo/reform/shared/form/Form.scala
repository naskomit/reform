package sysmo.reform.shared.form

import sysmo.reform.shared.gremlin.{tplight => TP}
import sysmo.reform.shared.gremlin.tplight.{gobject => GO}


abstract class FormElement[T <: GO.VertexDef](val ed: T) extends GO.VertexObj {
  type ED = T
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

abstract class FormRelation[T <: GO.EdgeDef](val ed: T) extends GO.EdgeObj {
  type ED = T
}

trait FormRelationCompanion[U] {
  /** The form relation type to which this is companion */
  type FRT = U
  trait IBuilder {
    protected val graph: TP.Graph
    val edge: TP.Edge
//    lazy val vertex: TP.Vertex = graph.add_vertex(Def.label)
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