package sysmo.reform.shared.util

import scala.collection.mutable
import scala.reflect.ClassTag

object Injector {
  val implementations: mutable.HashMap[String, AnyRef] = mutable.HashMap[String, AnyRef]()

  def configure[T <: AnyRef](t: T)(implicit ct: ClassTag[T]): Either[Throwable, Unit] = {
    val t_name = ct.runtimeClass.getName
    implementations.get(t_name) match {
      case Some(inst) => {
        val err = s"Instance for class ${t_name} already configured"
        System.err.println(err)
        Left(new IllegalStateException(err))
      }
      case None => {
        implementations.put(t_name, t)
        Right(())
      }
    }
  }

  def inject_option[T](implicit ct: ClassTag[T]): Either[Throwable, T] = {
    val t_name = ct.runtimeClass.getName
    implementations.get(t_name).toRight(
      new NoSuchElementException(s"There is no instance for type ${t_name}")
    ).flatMap {
      case inst: T => Right(inst)
      case inst => Left(new IllegalStateException(s"Incorrect instance type ${inst.getClass} for class ${t_name}"))
    }
  }
  def inject[T](implicit ct: ClassTag[T]): T = {
    inject_option match {
      case Left(err) => throw(err)
      case Right(value) => value
    }
  }
}
