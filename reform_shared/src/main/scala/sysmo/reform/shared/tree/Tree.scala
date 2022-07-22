package sysmo.reform.shared.tree

trait TreeTypes {
  type NodeId
  type ActionType
}

trait TreeNode[T] {
  type Id = Any
  def parent: Option[TreeNode[T]]
  def id: Id
  def name: String
  def icon: Option[String]
  //    def content: T
  def actions: Seq[NodeAction[T]]
  def dispatcher: Dispatcher[T]
  def is_selected: Boolean
}

trait TreeLeaf[T] extends TreeNode[T] {
}

trait TreeBranch[T] extends TreeNode[T] {
  def children: Seq[TreeNode[T]]
}

trait NodeAction[T] {
  def name: String
  def data: T
}

trait Dispatcher[T] {
  def dispatch[U <: T](action: U): Unit
  def select(id: Any): Unit
}

trait Renderer {
  def rerender(): Unit
}

trait Tree[T] {
  type NodeId = Any
  val dispatcher: Dispatcher[T]
  var renderer: Option[Renderer] = None
  def root: TreeNode[T]
  def node_is_selected(id: NodeId): Boolean
  def selection: Seq[NodeId]
}

