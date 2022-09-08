package sysmo.reform.shared.sources.property

import cats.MonadThrow
import sysmo.reform.shared.runtime.FLocal
import sysmo.reform.shared.sources.EditableSource
import sysmo.reform.shared.util.MonadicIterator

trait PropertyTypes {
  type Id
  type ActionType
}

trait PropertySource[T <: PropertyTypes, F[+_]] extends EditableSource[F] {
  implicit val mt: MonadThrow[F]
  def props: MonadicIterator[F, Property]
  def cache: F[LocalPropertySource[T, F]] =
    props.traverse(prop_list => LocalPropertySource(prop_list, this))
  def dispatcher: Dispatcher[T, F]
}

trait Dispatcher[T <: PropertyTypes, F[+_]] {
  type ActionType = T#ActionType
  def dispatch[U <: ActionType](action: U): F[Unit]
}

trait LocalPropertySource[T <: PropertyTypes, F[+_]] extends PropertySource[T, F] {
  def props_sync: Iterator[Property]
}

object LocalPropertySource {
  class LocalPropertySourceImpl[T <: PropertyTypes, F[+_]]
  (properties: Seq[Property], orig: PropertySource[T, F])(implicit val mt: MonadThrow[F])
    extends LocalPropertySource[T, F] {
    override def props: MonadicIterator[F, Property] = orig.props
    override def cache: F[LocalPropertySource[T, F]] = mt.pure(this)
    override def props_sync: Iterator[Property] = properties.iterator
    override def dispatcher: Dispatcher[T, F] = orig.dispatcher

  }

  def apply[T <: PropertyTypes, F[+_]]
  (prop_list: Seq[Property], orig: PropertySource[T, F])(implicit mt: MonadThrow[F]): LocalPropertySource[T, F] =
    new LocalPropertySourceImpl[T, F](prop_list, orig)
}