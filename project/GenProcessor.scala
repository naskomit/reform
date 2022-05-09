import io.apigee.trireme.node10.node.path
import sbt.{**, File, Glob, IO, pathToPathOps}
import sbt.internal.util.ManagedLogger
import sbt.nio.file.FileTreeView

import java.nio.file.Path
import scala.meta.{Input, Source, Defn, Term, Decl}
import scala.meta.quasiquotes._
import scala.util.Using

class GenProcessor(base_in: File, base_out: File, log: ManagedLogger) {
  def run: Seq[File] = {
    val g = Glob(base_in.getPath) / ** / "*.scala"
    val xformer = SourceTransformer()
    FileTreeView.default.list(g).map {case(path_in, attr) =>
      val rel_path = base_in.toPath.relativize(path_in)
      val file_in = path_in.toFile
      val path_out = base_out.toPath / rel_path.toString
      val file_out = path_out.toFile
      log.info(s"${path_in} ->  ${path_out}")
      process_file(file_in, file_out)
      file_out
    }
  }

  def process_file(file_in: File, file_out: File): Unit = {
    val input_text: String = IO.read(file_in)
    val input = Input.VirtualFile(file_in.getPath, input_text)
    val source_in: Source = input.parse[Source].get
    val source_out = transform_source_file(source_in)
    log.info(s"Input from ${file_in}")
    log.info(source_in.toString)
    log.info(s"Output to ${file_out}")
    log.info(source_out.toString)
    IO.write(file_out, source_out.toString())
  }

  def transform_source_file(source_in: Source): Source = {
    val source_out = source_in.copy(stats = source_in.stats.map {
      case trait_defn: Defn.Trait => trait_method_map(trait_defn)
    })
    source_out
  }

  def trait_method_map(td: Defn.Trait): Defn.Trait = {
    val methods: Seq[Term.Name] = td.templ.stats.map {
      case method_defn: Decl.Def => method_defn.name
    }

    //    Seq(...${methods.map(x => q"""(base_path :+ ${x.value}) -> 5""")})
    val map_entries = methods.map(
      (x: Term.Name) => q"""
        ((base_path :+ ${x.value}) -> 5)
      """
    ).toList
    println(map_entries)
    //          ${methods.map(x => q"""(base_path :+ $x) -> 5""")}: _*

    td.copy(templ = td.templ.copy(
      stats = td.templ.stats :+
        q"""
        val route_map = Map(...${List(map_entries)})
         """
    ))


  }

}

object GenProcessor {
  def apply(base_in: File, base_out: File, log: ManagedLogger): Seq[File] =
    new GenProcessor(base_in, base_out, log).run
}