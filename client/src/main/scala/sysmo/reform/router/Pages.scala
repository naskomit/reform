package sysmo.reform.router

import japgolly.scalajs.react.vdom.TagMod
import japgolly.scalajs.react.vdom.html_<^._
import sysmo.reform.components.ApplicationPanel

trait PageBase {
  def name : String
  def label: String = name
  def icon: String
  def panel: ApplicationPanel
}

trait Page extends PageBase {
  def url: String = "#" + name
}

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
      case x : Category => new PageCollection(x.children, None).collect
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
    new PageCollection(pages.toSeq, Some(home))
  }
}


