package sysmo.reform.shared.tree

object MTree {
  trait MNode[T] extends TreeNode[T] {
    var _parent: Option[TreeNode[T]] = None
    var _tree: Option[Tree[T]] = None
    override def parent: Option[TreeNode[T]] = _parent

    override def dispatcher: Dispatcher[T] = _tree.get.dispatcher

    def set_parents(tree: Tree[T], pv: Option[MNode[T]]): Unit = {
      _parent = pv
      _tree = Some(tree)
      //        pv match {
      //          case None => println(s"None --parent of-> ${name}")
      //          case Some(p) => println(s"${p.name} --parent of-> ${name}")
      //        }

      this match {
        case node: TreeLeaf[T] =>
        case node: TreeBranch[T] => node.children.foreach{c =>
          c.asInstanceOf[MNode[T]].set_parents(tree, Some(this))
        }
      }
    }

    override def is_selected: Boolean = _tree.get.node_is_selected(id)
  }

  case class MLeaf[T](id: Any, name: String, icon: Option[String] = None, actions: Seq[NodeAction[T]] = Seq()) extends MNode[T] with TreeLeaf[T] {
  }

  case class MBranch[T](id: Any, name: String, icon: Option[String], children: Seq[TreeNode[T]], actions: Seq[NodeAction[T]] = Seq()) extends MNode[T] with TreeBranch[T] {
  }

  case class MTree[T](root: MNode[T], multi_select: Boolean = false) extends Tree[T] {
    var _selection = Set[NodeId]()
    val dispatcher = new Dispatcher[T] {
      override def dispatch[U <: T](action: U): Unit = {
        println(s"[MTree/Action] ${action}")
        renderer.foreach(_.rerender())
      }

      override def select(id: NodeId): Unit = {
        if (multi_select) {
          if (selection.contains(id)) {
            _selection = _selection - id
          } else {
            _selection = _selection + id
          }
        } else {
          _selection = Set(id)
        }
        println(s"Current selection: $selection")
        renderer.foreach(_.rerender())
      }
    }

    override def selection: Seq[NodeId] = _selection.toSeq
    override def node_is_selected(id: Any): Boolean = selection.contains(id)
  }

  def apply[T](root: MNode[T]): MTree[T] = {
    val tree = new MTree(root)
    root.set_parents(tree, None)
    tree
  }

  case class MAction(name: String, data: String = "") extends NodeAction[String]
  case class InsertBefore(id: String) extends NodeAction[String] {
    override def name: String = "Insert before"
    override def data: String = s"$name($id)"
  }
  case class InsertAfter(id: String) extends NodeAction[String] {
    override def name: String = "Insert after"
    override def data: String = s"$name($id)"
  }
  case class Remove(id: String) extends NodeAction[String] {
    override def name: String = "Remove"
    override def data: String = s"$name($id)"
  }

  def array_actions(id: String): Seq[NodeAction[String]] =
    Seq(
      InsertBefore(id), InsertAfter(id), Remove(id)
    )

  def example1: MTree[String] = {
    val i_group = Option("fa fa-map")
    val i_array = Option("fa fa-list")
    apply[String](
      MBranch("0", "Root", i_group, Seq(
        MBranch("1", "Branch 1", i_group, Seq(
          MLeaf("11", "Leaf 11"),
          MBranch("12", "Branch 12", i_group, Seq(
            MLeaf("121", "Leaf 121"),
            MLeaf("122", "Leaf 122"),
          ))
        )),
        MBranch("2", "Branch 2", i_array, Seq(
          MLeaf("21", "Leaf 21", actions = array_actions("21")),
          MLeaf("22", "Leaf 22", actions = array_actions("22")),
          MLeaf("23", "Leaf 23", actions = array_actions("23")),
        ),
          actions = Seq(MAction("Append", "2"))
        )
      ))
    )
  }
}