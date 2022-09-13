package sysmo.reform.shared.sources.property

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.FLocal
import sysmo.reform.shared.sources.{SourceAction, Dispatcher, EditableSource}
import sysmo.reform.shared.util.MonadicIterator

trait PropertySource[F[+_]] extends EditableSource[F] {
  implicit val mt: MonadThrow[F]
  def props: MonadicIterator[F, Property]
  def cache: F[LocalPropertySource[F]] =
    props.traverse(prop_list => LocalPropertySource(prop_list, this))
}



trait LocalPropertySource[F[+_]] extends PropertySource[F] {
  def props_sync: Iterator[Property]
}

object LocalPropertySource {
  class LocalPropertySourceImpl[F[+_]]
  (properties: Seq[Property], orig: PropertySource[F])(implicit val mt: MonadThrow[F])
    extends LocalPropertySource[F] {
    override def props: MonadicIterator[F, Property] = orig.props
    override def cache: F[LocalPropertySource[F]] = mt.pure(this)
    override def props_sync: Iterator[Property] = properties.iterator
    override def dispatcher: Dispatcher[F] = orig.dispatcher

  }

  def apply[F[+_]]
  (prop_list: Seq[Property], orig: PropertySource[F])(implicit mt: MonadThrow[F]): LocalPropertySource[F] =
    new LocalPropertySourceImpl[F](prop_list, orig)
}


case class SetFieldValue(id: ObjectId, value: Value) extends SourceAction
