package sysmo.reform.db

object GremlinIO {



  import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}

  import org.apache.tinkerpop.gremlin.structure.io.graphson
  val GRAPHSON_READER_3_0 = graphson.GraphSONReader.build().create()
  val GRAPHSON_WRITER_3_0 = graphson.GraphSONWriter.build().create()
  def readValue[T](value: String, clazz: Class[_ <: T]): T = try {
    val in = new ByteArrayInputStream(value.getBytes("UTF-8"))
    try {
      GRAPHSON_READER_3_0.readObject(in, clazz)
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    } finally if (in != null) in.close()
  }

  def writeValueAsString(value: Any): String = try {
    val out = new ByteArrayOutputStream
    try {
      GRAPHSON_WRITER_3_0.writeObject(out, value)
      out.toString("UTF-8")
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    } finally if (out != null) out.close()
  }
}
