package sysmo.reform.query
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator
import org.apache.tinkerpop.gremlin.orientdb.{OrientGraph, OrientGraphFactory}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{GraphTraversalSource, __}
import org.apache.tinkerpop.gremlin.process.traversal.step.util.WithOptions
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Order, P, Traversal}
import org.apache.tinkerpop.gremlin.structure.T
import sysmo.reform.db.GremlinIO

import scala.collection.MapView
import scala.jdk.CollectionConverters._

class GremlinTest(graphFactory: OrientGraphFactory) {
  object TraversalHelpers {
    def with_traversal[A](f: GraphTraversalSource => A): A = {
      val graph = graphFactory.getNoTx
      val g = graph.traversal()
      val result = f(g)
      g.close()
      graph.close()
      result
    }

    case class ValueMap(x: MapView[String, Any]) {
      def get: MapView[String, Any] = x
      def get_option(k: String ): Option[Any] = x.get(k)

    }

    implicit class TraversalResult[S, E](x: Traversal[S, E]) {
      def as_value_map_seq  = x.asScala.map(x =>
        x.asInstanceOf[java.util.Map[String, Any]].asScala.toMap
      )
    }



  }

  import TraversalHelpers._
  def sort_bytecode(): Unit = {
    with_traversal(g => {
      val traversal = g.V().hasLabel("SocioDemographic")
        .order().by(__.coalesce(__.values("4a"), __.constant("")), Order.asc)
        .order().by(__.coalesce(__.values("4"), __.constant(78L)), Order.asc)
        .valueMap()
//      val prop_list = Seq("1a", "1b", "1c", "3", "4")
//      val code = g.V().hasLabel("SocioDemographic").has("1", 173).valueMap(prop_list: _*)
//        .by(__.unfold())
      println(GremlinIO.writeValueAsString(traversal.asAdmin.getBytecode))
      traversal.as_value_map_seq.foreach(println)

//      code.asScala.map(x => x.getOrDefault("4a")).toSeq.foreach(println)
//      var x: Option[Int]
    })
  }

  def run(): Unit = {
    sort_bytecode()
  }
}
