package sysmo.reform.shared.tree

trait TreeTypes {
  type NodeId
  type ActionType
}

trait TreeNode[T <: TreeTypes] {
  type Id = T#NodeId
  type ActionType = T#ActionType
  def parent: Option[TreeNode[T]]
  def id: Id
  def name: String
  def icon: Option[String]
  //    def content: T
  def actions: Seq[NodeAction[ActionType]]
  def dispatcher: Dispatcher[T]
  def is_selected: Boolean
}

trait TreeLeaf[T <: TreeTypes] extends TreeNode[T] {
}

trait TreeBranch[T <: TreeTypes] extends TreeNode[T] {
  def children: Seq[TreeNode[T]]
}

trait NodeAction[A] {
  def name: String
  def data: A
}

trait Dispatcher[T <: TreeTypes] {
  type ActionType = T#ActionType
  def dispatch[U <: ActionType](action: U): Unit
  def select(id: T#NodeId): Unit
}

trait Renderer {
  def rerender(): Unit
}

trait Tree[T <: TreeTypes] {
  type NodeId = T#NodeId
  type ActionType = T#ActionType
  val dispatcher: Dispatcher[T]
  var renderer: Option[Renderer] = None
  def root: TreeNode[T]
  def node_is_selected(id: NodeId): Boolean
  def selection: Seq[NodeId]
}

