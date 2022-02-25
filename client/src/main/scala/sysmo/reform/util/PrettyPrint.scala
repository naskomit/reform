package sysmo.reform.util

object PrettyPrint {
  def pprint(x : Any) = println(printers.any(x))

  private object printers {
    def any(x: Any): String = x match {
      case x: String => x
      case x: Boolean => x.toString
      case x: Int => x.toString
      case x: Double => x.toString
      case x: Enumeration#Value => x.toString
      case x: Iterable[_] => x.map(any).mkString(", ")
      case None => "None"
      case x: Product => product(x)
    }

    class Idented(var i: Int = 0) {
      def inc: String = {i = i + 1; ""}
      def dec: String = {i = i - 1; ""}
      def println(s: String): String = "  " * i + s + "\n"
      def println_item(s: String): String = "  " * i + s + ",\n"
    }

    val idented = new Idented

    def product(x: Product): String = {
      val fields = for ((id, value) <- x.productElementNames.zip(x.productIterator))
        yield f"$id -> ${any(value)}"
      idented.println(x.getClass.getSimpleName + "(") ++ idented.inc +
        fields.map(idented.println_item).mkString("") + idented.dec + idented.println(")")
    }
  }

}
