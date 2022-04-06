package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.coviddata.shared.{data => CD}
import sysmo.reform.shared.data.graph.{EdgeSchema, VertexSchema}

object CovidDatabaseSchema extends G.DatabaseSchema {
  val SocioDemographic: VertexClass = vertex_class(CD.SocioDemographic.schema)
  val Clinical: VertexClass = vertex_class(CD.Clinical.schema)
  val Therapy: VertexClass = vertex_class(CD.Therapy.schema)
  val Immunology:  VertexClass= vertex_class(CD.Immunology.schema)

  override val vertex_schemas: Seq[VertexClass] = Seq(
    SocioDemographic,
    Clinical,
    Therapy,
    Immunology
  )

  val HasClinical: EdgeClass = edge_class(
    G.Schema.edge_builder("HasClinical")
      .from(SocioDemographic, G.MultOne).to(Clinical, G.MultOne).build
  )

  val HasTherapy: EdgeClass = edge_class(
    G.Schema.edge_builder("HasTherapy")
      .from(SocioDemographic, G.MultOne).to(Therapy, G.MultOne).build
  )

  val HasImmunology: EdgeClass = edge_class(
    G.Schema.edge_builder("HasImmunology")
      .from(SocioDemographic, G.MultOne).to(Immunology, G.MultOne).build
  )

  override val edge_schemas: Seq[EdgeClass] = Seq(
    HasClinical,
    HasTherapy,
    HasImmunology
  )
}
