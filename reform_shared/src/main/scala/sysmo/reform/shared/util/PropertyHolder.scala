package sysmo.reform.shared.util

import scala.collection.mutable

case class Property[T](val name: String, val default: T) {
  def :=(v: T): (Property[T], Option[T]) = {
    (this, Some(v))
  }
  def clear: (Property[T], Option[T]) = (this, None)
//  def :=(v: Option[T]): Unit = {
//    container.foreach(c => c.data.removed(name))
//  }
}

//object Property {
//  def apply[T](name: String): Property[T] = new Property[T](name, None)
//  def apply[T](name: String, default: T): Property[T] = new Property[T](name, Some(default))
//}

trait PropertyHolder[S <: PropertyHolder[S]] {
  type Data = Map[String, Any]
  type Concrete = S
  type Props
  val props: Props
  private var data: Data  = Map[String, Any]()
  def fork: S
  def update(f_list: (Props => (Property[_], Option[_]))*): S = {
    update(PropertyHolder.update(props)(f_list: _*))
  }

  def update(u: PropertyHolder.Update[Props]): S = _update(u.key_values: _*)

  def _update(key_values: (Property[_], Option[_])*): S  = {
    val new_data = key_values.foldLeft(data){ (acc, kv) =>
      kv._2 match {
        case Some(v) => acc + (kv._1.name -> v)
        case None => acc.removed(kv._1.name)
      }
    }
    val new_instance = fork
    new_instance.data = new_data
    new_instance
  }

//  def get[T](p: Property[T]): T = data.getOrElse(p.name, p.default).asInstanceOf[T]
  def get[T](f: Props => Property[T]): T = {
    val p = f(props)
    data.getOrElse(p.name, p.default).asInstanceOf[T]
  }
}

object PropertyHolder {
  case class Update[Props](key_values: Seq[(Property[_], Option[_])])
  def update[Props](prop_obj: Props)(f_list: (Props => (Property[_], Option[_]))*): Update[Props] = {
    val updates: Seq[(Property[_], Option[_])] = f_list.map(f => f(prop_obj))
    Update(updates)
  }
}
