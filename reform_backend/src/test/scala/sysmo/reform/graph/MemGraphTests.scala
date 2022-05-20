package sysmo.reform.graph
import org.scalatest.funspec.AnyFunSpec
import sysmo.reform.shared.gremlin.tplight.algo.DependencyResolution
import sysmo.reform.shared.gremlin.tplight.{Direction, Edge, Graph, GraphTraversalBuilder, GraphTraversalSource, Vertex}

import scala.collection.mutable

class MemGraphTests extends AnyFunSpec {


  describe("A modern graph") {
    TestGraph.with_modern { case TestGraph(graph, vertex_map, edge_map) =>
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
    TestGraph.with_modern { case TestGraph(graph, vertex_map, edge_map) =>
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

      it("test map") {
        val t1 = g.V().map[String](x => x.get.value[String]("name").get).build
        assert(t1.toSet == Set("marko", "vadas", "lop", "josh", "ripple", "peter"))
        val t2 = g.V()
          .map(x => x.get.value[String]("lang").getOrElse("N/A"))
          .build
        assert(t2.toSet == Set("scala", "N/A"))
      }

      it("test flatMap") {
        // TODO
     }

      it("test filter") {
        val t1 = g.V().filter(x => x.get.value[String]("lang").isDefined).build
        assert(t1.size == 2)
      }

      it("test sideEffect") {
        val acc = mutable.HashSet[String]()
        val t1 = g.V()
          .map[String](x => x.get.value[String]("lang").getOrElse("N/A"))
          .sideEffect(x => acc.add(x.get)).build
        t1.foreach(x => 1)
        assert(acc.toSet == Set("scala", "N/A"))

      }
    }
  }

  describe("Finding dependencies") {
    TestGraph.with_dep { case TestGraph(graph, vertex_map, edge_map) =>
      val dep_res = new DependencyResolution(graph)
      def names1(vseq: Set[Vertex]): Set[String] = vseq.map(x => x.value[String]("name").get)
      def names2(vseq: Seq[Set[Vertex]]): Seq[Any] = vseq.map {x => names1(x)}
      it("resolution for node a") {
        val modified = Set(vertex_map("a"))
        val affected = dep_res.find_affected(modified)
        val resolution = dep_res.resolution_seq(modified)
        assert(names1(affected) == Set("b", "y1", "y2", "c", "d", "y3"))
        assert(names2(resolution) == Seq(Set("b"), Set("y1", "c"), Set("y2", "d"), Set("y3")))
      }

      it("resolution for node e") {
        val modified = Set(vertex_map("e"))
        val affected = dep_res.find_affected(modified)
        val resolution = dep_res.resolution_seq(modified)
        assert(names1(affected) == Set("y2", "c", "d", "y3"))
        assert(names2(resolution) == Seq(Set("c"), Set("y2", "d"), Set("y3")))
      }

      it("resolution for node a with edge predicate") {
        val modified = Set(vertex_map("a"))
        val edge_predicate = (e: Edge) => e.label != "dummy_edge"
        val affected = dep_res.find_affected(modified, edge_predicate)
        val resolution = dep_res.resolution_seq(modified, edge_predicate)
        assert(names1(affected) == Set("b", "c", "d", "y3"))
        assert(names2(resolution) == Seq(Set("b"), Set("c"), Set("d"), Set("y3")))
      }
    }
  }
}