package sysmo.reform.graph

import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.gremlin.tplight.{Edge, Vertex, Graph}

case class TestGraph(graph: Graph, vertex_map: Map[String, Vertex], edge_map: Map[String, Edge])

object TestGraph {
  def create_modern: TestGraph = {
    val graph = MemGraph()
    /** Create vertices */
    val marko = graph.add_vertex(
      "person",
      "name" -> "marko",
      "age" -> 29,
    )
    val vadas = graph.add_vertex(
      "person",
      "name" -> "vadas",
      "age" -> 27,
    )
    val lop = graph.add_vertex(
      "software",
      "name" -> "lop",
      "lang" -> "scala",
    )
    val josh = graph.add_vertex(
      "person",
      "name" -> "josh",
      "age" -> 32,
    )
    val ripple = graph.add_vertex(
      "software",
      "name" -> "ripple",
      "lang" -> "scala",
    )
    val peter = graph.add_vertex(
      "person",
      "name" -> "peter",
      "age" -> 35,
    )

    /** Create edges */
    val mv = marko.add_edge("knows", vadas, "weight" -> 0.5)
    val mj = marko.add_edge("knows", josh, "weight" -> 1.0)
    val ml = marko.add_edge("created", lop, "weight" -> 0.4)
    val jr = josh.add_edge("created", ripple, "weight" -> 1.0)
    val jl = josh.add_edge("created", lop, "weight" -> 0.4)
    val pl = peter.add_edge("created", lop, "weight" -> 0.2)

    val vertex_map = Map[String, Vertex](
      "marko" -> marko, "vadas" -> vadas, "lop" -> lop,
      "josh" -> josh, "ripple" -> ripple, "peter" -> peter
    )

    val edge_map = Map[String, Edge](
      "mv" -> mv, "mj" -> mj, "ml" -> ml,
      "jr" -> jr, "jl" -> jl, "pl" -> pl
    )

    TestGraph(graph, vertex_map, edge_map)
  }

  def with_modern(f: TestGraph => Unit): Unit = {
    val graph: TestGraph = create_modern
    f(graph)
  }

  /** Dependency graph */
  def create_dep_graph: TestGraph = {
    val graph = MemGraph()
    val x1 = graph.add_vertex("node", ("name" -> "x1"))
    val x2 = graph.add_vertex("node", ("name" -> "x2"))
    val va = graph.add_vertex("node", ("name" -> "a"))
    val vb = graph.add_vertex("node", ("name" -> "b"))
    val vc = graph.add_vertex("node", ("name" -> "c"))
    val vd = graph.add_vertex("node", ("name" -> "d"))
    val ve = graph.add_vertex("node", ("name" -> "e"))
    val y1 = graph.add_vertex("node", ("name" -> "y1"))
    val y2 = graph.add_vertex("node", ("name" -> "y2"))
    val y3 = graph.add_vertex("node", ("name" -> "y3"))
    val z1 = graph.add_vertex("node", ("name" -> "z1"))
    val z2 = graph.add_vertex("node", ("name" -> "z2"))
    val w1 = graph.add_vertex("node", ("name" -> "w1"))

    val vertex_map = Map[String, Vertex](
      "a" -> va, "b" -> vb, "c" -> vc,
      "d" -> vd, "e" -> ve, "x1" -> x1, "x2" -> x2, "y1" -> y1,
      "y2" -> y2, "y3" -> y3, "z1" -> z1, "z2" -> z2, "w1" -> w1
    )

    val x1_va = x1.add_edge("edge", va, ("name" -> "x1_va"))
    val x2_va = x2.add_edge("edge", va, ("name" -> "x2_va"))
    val va_vb = va.add_edge("edge", vb, ("name" -> "va_vb"))
    val va_vd = va.add_edge("edge", vd, ("name" -> "va_vd"))
    val vb_y1 = vb.add_edge("dummy_edge", y1, ("name" -> "vb_y1"))
    val vb_vc = vb.add_edge("edge", vc, ("name" -> "vb_vc"))
    val vc_y2 = vc.add_edge("dummy_edge", y2, ("name" -> "vc_y2"))
    val vc_vd = vc.add_edge("edge", vd, ("name" -> "vc_vd"))
    val vd_y3 = vd.add_edge("edge", y3, ("name" -> "vd_y3"))
    val ve_vc = ve.add_edge("edge", vc, ("name" -> "ve_vc"))
    val z1_z2 = z1.add_edge("edge", z2, ("name" -> "z1_z2"))

    val edge_map = Map[String, Edge](
      "x1_va" -> x1_va, "x2_va" -> x2_va, "va_vb" -> va_vb,
      "va_vd" -> va_vd,  "vb_y1" -> vb_y1, "vb_vc" -> vb_vc,
      "vc_y2" -> vc_y2, "vc_vd" -> vc_vd, "vd_y3" -> vd_y3,
      "ve_vc" -> ve_vc, "z1_z2" -> z1_z2
    )

    TestGraph(graph, vertex_map, edge_map)
  }

  def with_dep(f: TestGraph => Unit): Unit = {
    val graph: TestGraph = create_dep_graph
    f(graph)
  }

}
