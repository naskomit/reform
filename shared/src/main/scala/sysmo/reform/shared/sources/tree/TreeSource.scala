package sysmo.reform.shared.sources.tree

import sysmo.reform.shared.sources.EditableSource
import sysmo.reform.shared.util.MonadicIterator

trait TreeTypes {
  type NodeId
  type ActionType
}

sealed trait TreeNode[T <: TreeTypes, F[+_]] {
  type Id = T#NodeId
  type ActionType = T#ActionType
  type Action = NodeAction[ActionType]
  def parent: F[Option[TreeNode[T, F]]]
  def id: Id
  def name: String
  def icon: Option[String]
  //    def content: T
  def actions: Seq[Action]
  def dispatcher: Dispatcher[T, F]
  def is_selected: Boolean
}

trait TreeLeaf[T <: TreeTypes, F[+_]] extends TreeNode[T, F] {
}

trait TreeBranch[T <: TreeTypes, F[+_]] extends TreeNode[T, F] {
  def children: MonadicIterator[F, TreeNode[T, F]]
}

trait NodeAction[A] {
  def name: String
  def data: A
}

trait Dispatcher[T <: TreeTypes, F[+_]] {
  type ActionType = T#ActionType
  def dispatch[U <: ActionType](action: U): F[Unit]
  def select(id: T#NodeId): Unit
}

trait Renderer {
  def rerender(): Unit
}

trait TreeSource[T <: TreeTypes, F[+_]] extends EditableSource[F] {
  type NodeId = T#NodeId
  type ActionType = T#ActionType
  val dispatcher: Dispatcher[T, F]
  var renderer: Option[Renderer] = None
  def root: TreeNode[T, F]
  def node_is_selected(id: NodeId): Boolean
  def selection: Set[NodeId]
}

