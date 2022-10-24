package sysmo.reform.shared.sources.property

import cats.MonadThrow
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.sources.{Dispatcher, EditableSource, SourceAction}
import sysmo.reform.shared.util.{MonadicIterator, containers}

trait PropertySource[F[+_]] extends EditableSource[F] {
  implicit val mt: MonadThrow[F]
  val id: ObjectId
  def props: MonadicIterator[F, Property]
  def cache: F[LocalPropertySource[F]] =
    props.traverse(prop_list => LocalPropertySource(id, prop_list, this))
}

trait LocalPropertySource[F[+_]] extends PropertySource[F] {
  def props_sync: Iterator[Property]
}

object LocalPropertySource {
  class LocalPropertySourceImpl[F[+_]]
  (val id: ObjectId, properties: Seq[Property], orig: PropertySource[F])(implicit val mt: MonadThrow[F])
    extends LocalPropertySource[F] {
    override def props: MonadicIterator[F, Property] = orig.props
    override def cache: F[LocalPropertySource[F]] = mt.pure(this)
    override def props_sync: Iterator[Property] = properties.iterator
    override def dispatcher: Dispatcher[F] = orig.dispatcher

  }

  def apply[F[+_]]
  (id: ObjectId, prop_list: Seq[Property], orig: PropertySource[F])(implicit mt: MonadThrow[F]): LocalPropertySource[F] =
    new LocalPropertySourceImpl[F](id, prop_list, orig)
}

