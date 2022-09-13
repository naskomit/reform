package sysmo.reform.shared.sources.tree

import sysmo.reform.shared.data.ObjectId
import sysmo.reform.shared.sources.{Dispatcher, EditableSource, SourceAction}
import sysmo.reform.shared.util.MonadicIterator

sealed trait TreeNode[F[+_]] {
  type ActionType = SourceAction
  type Action = NodeAction[ActionType]
  def parent: F[Option[TreeNode[F]]]
  def id: ObjectId
  def name: String
  def icon: Option[String]
  //    def content: T
  def dispatcher: Dispatcher[F]
  def actions: Seq[Action]
  def is_selected: Boolean
}

trait TreeLeaf[F[+_]] extends TreeNode[F] {
}

trait TreeBranch[F[+_]] extends TreeNode[F] {
  def children: MonadicIterator[F, TreeNode[F]]
}

trait NodeAction[A] {
  def name: String
  def data: A
}

trait Renderer {
  def rerender(): Unit
}

trait TreeSource[F[+_]] extends EditableSource[F] {
  type ActionType = SourceAction
  var renderer: Option[Renderer] = None
  def root: TreeNode[F]
  def node_is_selected(id: ObjectId): Boolean
  def selection: Set[ObjectId]
}

