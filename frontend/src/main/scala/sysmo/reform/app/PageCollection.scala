package sysmo.reform.app

import japgolly.scalajs.react.vdom.TagMod
import japgolly.scalajs.react.vdom.html_<^._

trait PageBase {
  val name: String
  val label: Option[String]
  val icon: String
  def make_label: String = label.getOrElse(name)
  var path: String = ""
}

trait Page extends PageBase {
  def url: String = "#" + name
  val panel: Panel
}

case class SimplePage(name: String, label: Option[String], icon: String, panel: Panel) extends Page

trait Category extends PageBase {
  def children: Seq[PageBase]
}

class PageCollection(pages: Seq[PageBase], _home: Option[Page]) {
  def home: Page = _home match {
    case Some(x: Page) => x
    case _ => throw new IllegalArgumentException("No home page defined")
  }

  // pred: Page => Boolean
  def collect: Seq[Page] = {
    pages.flatMap {
      case x : Page  => Seq(x)
      case x : Category =>
        new PageCollection(x.children, None).collect
    }
  }

  def compute_path(parent_path: String): Unit = {
    pages.foreach {
      case x: Page => x.path = parent_path + "." + x.name
      case x: Category => {
        x.path = parent_path + "." + x.name
        new PageCollection(x.children, None).compute_path(x.path)
      }
    }

  }

  def html(f: Page => TagMod)(g: (Category, TagMod) => TagMod): TagMod = {
    pages.map {
      case x: Page => f(x)
      case x: Category => {
        val child_tag_mod = new PageCollection(x.children, None).html(f)(g)
        g(x, child_tag_mod)
      }
    }.toTagMod
  }
}

object PageCollection {
  def apply(pages: PageBase*): PageCollection = {
    val home = pages.head match {
      case x: Page => x
      case _ => throw new IllegalArgumentException("The first item of the PageCollection should be the home page")
    }
    val coll = new PageCollection(pages, Some(home))
    coll.compute_path("Root")
    coll
  }
}