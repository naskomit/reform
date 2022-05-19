package sysmo.reform.graph
import org.scalatest.funspec.AnyFunSpec
import sysmo.reform.shared.gremlin.memg.MemGraph
import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalBuilder, Vertex}

case class TestGraph(graph: Graph, vertex_map: Map[String, Vertex], edge_map: Map[String, Edge])

class MemGraphTests extends AnyFunSpec {
  def create_modern(): TestGraph = {
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
    val graph: TestGraph = create_modern()
    f(graph)
  }

  describe("A modern graph") {
    with_modern { case TestGraph(graph, vertex_map, edge_map) =>
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

      it("vertex josh should have 1 incoming and 2 outgoing edges") {
        val josh = vertex_map("josh")
        assert(josh.edges(Direction.IN, Seq()).size == 1)
        assert(josh.edges(Direction.OUT, Seq()).size == 2)
        assert(josh.edges(Direction.BOTH, Seq()).size == 3)

        assert(josh.edges(Direction.IN, Seq("knows")).size == 1)
        assert(josh.edges(Direction.OUT, Seq("knows")).isEmpty)
        assert(josh.edges(Direction.IN, Seq("created")).isEmpty)
        assert(josh.edges(Direction.OUT, Seq("created")).size == 2)

        assert(josh.vertices(Direction.OUT, Seq()).size == 2)
        assert(josh.vertices(Direction.IN, Seq()).map(_.value[String]("name").get).toSet == Set("marko"))
        assert(josh.vertices(Direction.OUT, Seq()).map(_.value[String]("name").get).toSet == Set("lop", "ripple"))
      }
    }

  }

  describe("A modern graph traversal") {
    with_modern { case TestGraph(graph, vertex_map, edge_map) =>
      val g = graph.traversal()
      it("should get vertices 1, 3") {
        val t1 = g.V(1, 3).build
        assert(t1.map(x => x.id).toSeq == Seq(1, 3))
      }

      it("should get names of vertices 2, 4") {
        val t1 = g.V(2, 4).valueMap[Any]().build
        assert(t1.map(x => x("name")).toSeq == Seq("vadas", "josh"))
      }

      it("vertex josh should have 1 incoming and 2 outgoing edges") {
        val t1 = g.V(4).valueMap[Any]().build
        assert(t1.next()("name") == "josh")
        val t2 = g.V(4).out().valueMap[Any]().build
        assert(t2.map(x => x("name")).toSet == Set("ripple", "lop"))
        val t3 = g.V(4).outE().inV().valueMap[Any]("name").build
        assert(t3.map(x => x("name")).toSet == Set("ripple", "lop"))
        val t4 = g.V(4).outE().outV().valueMap[Any]("name").build
        assert(t4.map(x => x("name")).toSet == Set("josh"))
      }

      it("edges mv & mj should have weights 0.5 & 1.0") {
        val t1 = g.V(1).outE("knows").valueMap[Any]().build
        assert(t1.map(_("weight")).toSet == Set(0.5, 1.0))
      }
    }
  }
}