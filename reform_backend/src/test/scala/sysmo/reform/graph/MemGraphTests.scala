package sysmo.reform.graph
import org.scalatest.funspec.AnyFunSpec
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.gremlin.tplight.Graph

class MemGraphTests extends AnyFunSpec {
  def create_modern(): Graph = {
    import sysmo.reform.shared.gremlin.tplight.PropId._
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

  def with_modern(f: Graph => Unit): Unit = {
    val graph: Graph = create_modern()
    f(graph)
  }

  describe("A modern graph") {
    with_modern { graph =>
      it("should have 6 vertices") {
        assert(graph.vertices().size == 6)
      }
      it("should have 6 edges") {
        assert(graph.edges().size == 6)
      }
      it("vertex 1 should have keys") {
        assert(graph.vertices(1).flatMap(_.keys).toSet[String] == Set("name", "age"))
      }
      it("vertices 1,3 should have names (\"marko\", \"lop\")") {
        assert(
          graph.vertices(1, 3).map(_.property("name").value).toSet
            ==
            Set("marko", "lop").map(Some(_)), "Incorrect keys"
        )
      }
    }

  }

  describe("A modern graph traversal") {
    with_modern { graph =>
      val g = graph.traversal()
      it("should get vertex 1") {
        val t1 = g.V(1)
        t1.foreach(println)
      }
      it("") {

      }
    }
  }
}