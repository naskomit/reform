package sysmo.reform.shared.data.graph.tplight

trait Traversal[E] extends Iterator[E] {

}

case class Path(objects: Seq[Any], labels: Seq[Set[String]]) {

}

trait Traverser[T] {
  def get: T
  def path: Path
}

trait Step[E] extends Iterator[Traverser[E]] {

}

case class GraphTraversal[E]() extends Traversal[E] {
  def add_step[E2](s: Step[E2]): GraphTraversal[E2] = {
    new GraphTraversal[E2]
  }

  override def hasNext: Boolean = ???

  override def next(): E = ???
}
