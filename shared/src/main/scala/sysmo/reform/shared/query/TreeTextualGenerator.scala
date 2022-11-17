package sysmo.reform.shared.query

import scala.collection.mutable

sealed trait Enclosure {
  val enter: String
  val exit: String
}

object Enclosure {
  object None extends Enclosure {
    val enter: String = ""
    val exit: String = ""
  }
  object Brackets extends Enclosure {
    val enter: String = "("
    val exit: String = ")"
  }
}

sealed trait TextualTreeNode {
  def has_children: Boolean = true
}
object TextualTreeNode {
  def leaf(value: String): TextualTreeLeaf = {
    TextualTreeLeaf(value)
  }

  def leaf(value: Int): TextualTreeLeaf = {
    TextualTreeLeaf(value.toString)
  }

  def node(sep: String, enclosure: Enclosure = Enclosure.None): TextualTreeInternalNode = {
    TextualTreeInternalNode(Seq(), sep, enclosure)
  }

  lazy val empty: TextualTreeNode = node("")
}
case class TextualTreeLeaf(value: String) extends TextualTreeNode
case class TextualTreeInternalNode(var children: Seq[TextualTreeNode], sep: String, enclosure: Enclosure)
  extends TextualTreeNode {
  def +=(child: TextualTreeNode): this.type = {
    children = children :+ child
    this
  }
  override def has_children: Boolean = children.nonEmpty
}

class TreeTextualGenerator(val root: TextualTreeNode) {
  type Builder = mutable.StringBuilder

  def generate_node(node: TextualTreeNode, builder: Builder): Unit = {
    node match {
      case TextualTreeLeaf(value) => builder.append(value)
      case TextualTreeInternalNode(children, sep, enclosure) => {
        if (node != root) {
          builder.append(enclosure.enter)
        }
        children.zipWithIndex.foreach {
          case (child, index) => {
            generate_node(child, builder)
            if (index < children.length - 1) {
              builder.append(sep)
            }
          }
        }
        if (node != root) {
          builder.append(enclosure.exit)
        }
      }
    }
  }

  def generate(): String = {
    val builder: Builder = new mutable.StringBuilder
    generate_node(root, builder)
    builder.toString
  }
}

object TreeTextualGenerator {
  def apply(root: TextualTreeNode): TreeTextualGenerator =
    new TreeTextualGenerator(root)
  def apply(sep: String, enclosure: Enclosure = Enclosure.None): TreeTextualGenerator =
    new TreeTextualGenerator(TextualTreeInternalNode(Seq(), sep, enclosure))
}

