import sbt.AutoPlugin

object CodeGenerationPlugin extends AutoPlugin {
  //  object autoImport {
  //    val gen_input = taskKey[String]("Input folder")
  //    val gen_output = taskKey[String]("Input folder")
  //    val gen_code = taskKey[Unit]("Generates code")
  //  }
  //
  //  import autoImport._
  //  override def trigger = noTrigger
  //
  ////  override def requires = super.requires && DockerPlugin
  //  override lazy val projectSettings: Seq[Setting[_]] = Seq(
  //    gen_code := {
  //      val log = streams.value.log
  //      log.info(s"Generating code using input from ${gen_input.value}")
  //      val content = GenProcessor.read_file(gen_input.value)
  //      log.info(content.toString())
  //    }
  //  )
}
