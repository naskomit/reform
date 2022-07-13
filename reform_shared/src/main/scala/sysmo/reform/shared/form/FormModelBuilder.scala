package sysmo.reform.shared.form

import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.form.{runtime => FR}
import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.gremlin.{tplight => TP}

import scala.concurrent.ExecutionContext

trait FormModelBuilder {
  type BuildGraph = TP.Graph
  type Runtime = FR.FormRuntime
  var build_graph: BuildGraph = null
  var runtime: Runtime = null
  val name: String

  def field_group(name: String): FB.FieldGroup.Builder =
    FB.FieldGroup.builder(build_graph, name)

  def union(name: String, subtype1: FB.FieldGroup.Builder, other_subtypes: FB.FieldGroup.Builder*): FB.GroupUnion.Builder =
    subtype1.union(name, x =>
      other_subtypes.foldLeft(x)(
        (acc: FB.GroupUnion.Builder, group: FB.FieldGroup.Builder) => acc | group
      )
    )

  def create(build_graph: BuildGraph, runtime: Runtime): FR.Group

  def build()(implicit ec: ExecutionContext): FR.Group = {
    val build_graph = MemGraph()
    val runtime = FR.FormRuntime(build_graph)
    build(build_graph, runtime)
  }

  def build(build_graph: BuildGraph, runtime: Runtime): FR.Group = {
    this.build_graph = build_graph
    this.runtime = runtime
    create(build_graph, runtime)
  }
}

trait FormModelHelper {
  type Builder <: FormModelBuilder
  def builder: Builder
}