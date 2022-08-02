package sysmo.reform.shared.form.runtime

import sysmo.reform.shared.form.{build => FB}
import sysmo.reform.shared.tree.NodeAction
import sysmo.reform.shared.{tree => T}
import sysmo.reform.shared.{expr => E}
import sysmo.reform.shared.util.LabeledValue

object TreeView {
  class FTypes extends T.TreeTypes {
    override type NodeId = ObjectId
    override type ActionType = FormAction
  }

  implicit class RuntimeObjectAsTree(obj: RuntimeObject) extends T.Tree[FTypes] {
    var _selection: Set[NodeId] = Set[NodeId]()

    private var _dispatcher = new T.Dispatcher[FTypes] {
      override def dispatch[U <: ActionType](action: U): Unit = {
        println(s"Dispatch $action")
        obj.runtime.dispatch(action)
        renderer.foreach(_.rerender())
      }

      override def select(id: NodeId): Unit = {
        _selection = Set(id)
        renderer.foreach(_.rerender())
      }
    }

    override def selection: Set[NodeId] = _selection
    override val dispatcher: T.Dispatcher[FTypes] = _dispatcher
    override def root: T.TreeNode[FTypes] = as_node(obj, None)
    override def node_is_selected(id: NodeId): Boolean = selection.contains(id)

    def as_node(obj: RuntimeObject, name: Option[String]): T.TreeNode[FTypes] = {
      obj match {
        case v: AtomicValue => EmptyNode
        case group: Group => new GroupAsNode(group, name.getOrElse(group.prototype.symbol))
        case array: Array =>
          new ArrayAsNode(array, name.getOrElse(array.prototype.symbol) + s"(${array.count})")
        case ref: Reference => EmptyNode
      }
    }

    object EmptyNode extends T.TreeNode[FTypes] {
      override def parent: Option[T.TreeNode[FTypes]] = None
      override def id: Id = ObjectId.empty
      override def name: String = "Empty"
      override def icon: Option[String] = None
      override def actions: Seq[Action] = Seq()
      override def dispatcher: T.Dispatcher[FTypes] = _dispatcher
      override def is_selected: Boolean = false
    }

    trait ObjectAsNode[U <: RuntimeObject] extends T.TreeBranch[FTypes] {
      val obj: U
      override def id: Id = obj.id
      override def dispatcher: T.Dispatcher[FTypes] = _dispatcher
      override def is_selected: Boolean = node_is_selected(id)
      override def parent: Option[T.TreeNode[FTypes]] =
        obj.parent.map(x => as_node(x, None))

    }

    class GroupAsNode(val obj: Group, val name: String) extends ObjectAsNode[Group] {
      override def icon: Option[String] = Some("fa fa-map")
      override def actions: Seq[Action] = Seq()
      override def children: Seq[T.TreeNode[FTypes]] =
        obj.element_iterator
          .filterNot(x => x.target.isInstanceOf[AtomicValue])
          .map(x => as_node(x.target, Some(x.relation.descr)))
          .filterNot(x => x == EmptyNode)
          .toSeq
    }

    class ArrayAsNode(val obj: Array, val name: String) extends ObjectAsNode[Array] {
      override def icon: Option[String] = Some("fa fa-list")

      val append_actions: Seq[Action] = obj.prototype.prototype match {
        case union: FB.GroupUnion => union.subtypes.map(g =>
          new NodeAction[ActionType] {
            override def name: String = s"Append ${g.symbol}"
            override def data: ActionType = new AppendElement(obj.id, Some(g.symbol))
          }
        )
        case group: FB.FieldGroup => Seq(new NodeAction[ActionType] {
          override def name: String = s"Append ${group.symbol}"
          override def data: ActionType = new AppendElement(obj.id, Some(group.symbol))
        })
      }

      override def actions: Seq[Action] = append_actions
      override def children: Seq[T.TreeNode[FTypes]] = {
        def evaluate_label(e: E.Expression, group: Group): Option[String] = {
          E.Expression.eval(e, group.as_context) match {
            case Right(SomeValue(LabeledValue(v: String, _))) => Some(v)
            case x => {println(x); None}
          }
        }

        obj.element_iterator
          .zipWithIndex
          .map { case (group, i) =>
            val label: Option[String] = obj.prototype.label_expr
              .flatMap(e => evaluate_label(e, group))
            .orElse(group.prototype.label_expr.flatMap(e =>
              evaluate_label(e, group)
            )).orElse(Some((i + 1).toString))

            as_node(group, label)
          }
          .filterNot(x => x == EmptyNode)
          .toSeq
      }
    }

  }


}
