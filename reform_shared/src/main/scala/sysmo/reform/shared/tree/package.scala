package sysmo.reform.shared

package object tree {
  sealed trait TreeNode[T] {
    def parent: Option[TreeNode[T]]
    def id: Any
    def name: String
    def icon: Option[String]
//    def content: T
    def actions: Seq[NodeAction[T]]
    val dispatcher: Dispatcher[T]
  }

  trait TreeLeaf[T] extends TreeNode[T] {
  }

  trait TreeBranch[T] extends TreeNode[T] {
    def children: Seq[TreeNode[T]]
  }

  trait Tree[T] {
    def root: TreeNode[T]
  }

  trait NodeAction[T] {
    def name: String
    def data: T
  }

  trait Dispatcher[T] {
    def dispatch[U <: T](action: U): Unit
  }



  object MTree {
    trait MNode[T] extends TreeNode[T] {
      var _parent: Option[TreeNode[T]] = None
      override def parent: Option[TreeNode[T]] = _parent
      def set_parents(pv: Option[MNode[T]]): Unit = {
        _parent = pv
        pv match {
          case None => println(s"None --parent of-> ${name}")
          case Some(p) => println(s"${p.name} --parent of-> ${name}")
        }

        this match {
          case node: TreeLeaf[T] =>
          case node: TreeBranch[T] => node.children.foreach{c =>
            c.asInstanceOf[MNode[T]].set_parents(Some(this))
          }
        }
      }
      val dispatcher = new Dispatcher[T] {
        override def dispatch[U <: T](action: U): Unit =
          println(s"[MTree/Action] ${action}")
      }

    }

    case class MLeaf[T](id: Any, name: String, icon: Option[String] = None, actions: Seq[NodeAction[T]] = Seq()) extends MNode[T] with TreeLeaf[T] {
    }

    case class MBranch[T](id: Any, name: String, icon: Option[String], children: Seq[TreeNode[T]], actions: Seq[NodeAction[T]] = Seq()) extends MNode[T] with TreeBranch[T] {
    }

    case class MTree[T](root: MNode[T]) extends Tree[T]

        def apply[T](root: MNode[T]): MTree[T] = {
        root.set_parents(None)
        new MTree(root)
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
      MTree[String](
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
}
