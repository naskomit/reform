package sysmo.reform.util

import sysmo.reform.util.log.Logging
import scala.collection.mutable
import scala.reflect.ClassTag

trait TypeSingleton[A[_ <: B], B] extends Logging {
  private val instances : mutable.Map[ClassTag[_], A[_]] = mutable.Map()
  def create_instance[U <: B](implicit tag: ClassTag[U]): A[U]
  def get_instance[U <: B](tag: ClassTag[U]): A[U] = instances.getOrElseUpdate(
    tag, {
      logger.info(s"Creating component for ${tag.runtimeClass}")
      create_instance[U](tag)
    }
  ).asInstanceOf[A[U]]
}