package sysmo.reform.components.diagrams.mermaid

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object MermaidFacades {

  type InsertFn = js.Function1[String, Unit]

  @js.native
  trait MermaidAPI extends js.Object {
    val render: js.Function3[String, String, InsertFn, Unit] = js.native
  }

  @JSImport("mermaid", JSImport.Default)
  @js.native
  object Mermaid extends js.Object {
    val mermaidAPI: MermaidAPI = js.native
  }

  def show() = {
    println("==================== Mermaid ====================")
    dom.console.log(Mermaid)
    val container_id = "mermaid"
    val graph_definition = "graph TB\na-->b"
    val graph = Mermaid.mermaidAPI.render(
      container_id, graph_definition, (svg_code : String) => {
        println(svg_code)
      }
    )

  }
//  @js.native()
//  trait
}
