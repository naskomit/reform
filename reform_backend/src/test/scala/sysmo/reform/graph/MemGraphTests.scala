package sysmo.reform.graph

import sysmo.reform.shared.data.graph.tplight._
import sysmo.reform.shared.data.graph.memg.MemGraph

object MemGraphTests extends App {
  def create_modern(): Graph[Int] = {
    import PropId._
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
    marko.add_edge("knows", vadas, "weight" -> 0.5)
    marko.add_edge("knows", vadas, "weight" -> 1.0)
    marko.add_edge("created", lop, "weight" -> 0.4)
    josh.add_edge("created", ripple, "weight" -> 1.0)
    josh.add_edge("created", lop, "weight" -> 0.4)
    peter.add_edge("created", lop, "weight" -> 0.2)

    graph
  }

  def test_graph(): Unit = {
    val graph: Graph[Int] = create_modern()
    println(graph.vertices().toSeq)
    println(graph.edges().toSeq)
    println(graph.vertices(1))
  }

  test_graph()
}
