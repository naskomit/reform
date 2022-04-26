package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.coviddata.shared.{data => CD}
import sysmo.reform.shared.data.graph.{EdgeSchema, VertexSchema}

object CovidDatabaseSchema extends G.DatabaseSchema {
  val SocioDemographic: G.VertexClass = vertex_class(CD.SocioDemographic.schema)
  val Clinical: G.VertexClass = vertex_class(CD.Clinical.schema)
  val ClinicalLab: G.VertexClass = vertex_class(CD.ClinicalLab.schema)
  val Therapy1: G.VertexClass = vertex_class(CD.Therapy1.schema)
  val Therapy2: G.VertexClass = vertex_class(CD.Therapy2.schema)
  val TherapyLab: G.VertexClass = vertex_class(CD.TherapyLab.schema)
  val Immunology:  G.VertexClass= vertex_class(CD.Immunology.schema)

  override val vertex_schemas: Seq[G.VertexClass] = Seq(
    SocioDemographic,
    Clinical,
    ClinicalLab,
    Therapy1,
    Therapy2,
    TherapyLab,
    Immunology,
  )

  val HasClinical: G.EdgeClass = edge_class(
    G.Schema.edge_builder("HasClinical")
      .from(SocioDemographic, G.MultOne).to(Clinical, G.MultOne).build
  )

  val HasClinicalLab: G.EdgeClass = edge_class(
    G.Schema.edge_builder("HasClinicalLab")
      .from(SocioDemographic, G.MultMany).to(ClinicalLab, G.MultOne).build
  )

  val HasTherapy1: G.EdgeClass = edge_class(
    G.Schema.edge_builder(name = "HasTherapy1")
      .from(SocioDemographic, G.MultMany).to(Therapy1, G.MultOne)
      .build
  )

  val HasTherapy2: G.EdgeClass = edge_class(
    G.Schema.edge_builder("HasTherapy2")
      .from(SocioDemographic, G.MultOne).to(Therapy2, G.MultOne).build
  )

  val HasTherapyLab: G.EdgeClass = edge_class(
    G.Schema.edge_builder(name = "HasTherapyLab")
      .from(SocioDemographic, G.MultMany).to(TherapyLab, G.MultOne)
      .build
  )



  val HasImmunology: G.EdgeClass = edge_class(
    G.Schema.edge_builder("HasImmunology")
      .from(SocioDemographic, G.MultOne).to(Immunology, G.MultOne).build
  )

  override val edge_schemas: Seq[G.EdgeClass] = Seq(
    HasClinical,
    HasClinicalLab,
    HasTherapy1,
    HasTherapy2,
    HasTherapyLab,
    HasImmunology
  )
}
