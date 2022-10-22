package sysmo.reform.shared.runtime

import cats.MonadThrow
import cats.implicits._
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.types.{ArrayType, AtomicDataType, DataType, RecordType, UnionType}

class Instantiation[F[+_]](runtime: RFRuntime[F]) {
  implicit val mt: MonadThrow[F] = runtime.mt
//  implicit class DefaultMonadicFor[A](fa: F[A]) {
//    def withFilter(f: A => Boolean): F[A] = fa
//  }
  trait InstanceBuilder {
    def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RFObject[F]]
  }

  case class AtomicBuilder(value: Value)
    extends InstanceBuilder {
    override def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[AtomicObject[F]] = {
      lbound match {
        case lb: AtomicDataType => runtime.create_object(id => runtime.constructors.atomic(lb, id, value, parent))
        case _ => runtime.mt.raiseError(new IllegalArgumentException("Lower bound for AtomicObject should be of type AtomicDataType"))
      }
    }
  }

  case class RecordBuilder(dtype: RecordType, children: Seq[(String, InstanceBuilder)])
    extends InstanceBuilder {

    override def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RecordObject[F]] = {
      lbound match {
        case lb: RecordType => {
          if (dtype != lb) {
            return mt.raiseError(new IllegalArgumentException(
              s"Record instance type ${dtype.symbol} does not match type bound ${lb.symbol}"
            ))
          }
        }

        case lb: UnionType => {
          if (!lb.supertype_of(dtype)) {
            return mt.raiseError(new IllegalArgumentException(
              s"Type ${dtype.symbol} is not subtype of ${lb.symbol}"
            ))
          }
        }

        case lb => return mt.raiseError(new IllegalArgumentException(
          s"Expected object with bound ${lb}, but got Record(${dtype.symbol})"
        ))
      }

      val child_map = children.toMap
      val illegal_keys = child_map.keys.toSet.diff(dtype.fields.map(_.name).toSet)
      if (illegal_keys.nonEmpty) {
        return mt.raiseError(new IllegalArgumentException(
          s"No such fields $illegal_keys in group ${dtype.symbol}"
        ))
      }

      for {
        empty_instance <- runtime.create_object(id => runtime.constructors.record(dtype, id, parent))

        field_instances <- {
          dtype.fields.traverse { field =>
            val child_instance = child_map.get(field.name) match {
              case Some(v) => v.build(field.dtype, Some(empty_instance.id), runtime)
              case None => Defaults.create(field.dtype, Some(empty_instance.id), runtime)
            }
            child_instance.map(inst => (field.name, inst))
          }
        }

        instance <- field_instances.traverse {finst =>
            empty_instance.set_field(finst._1, finst._2.id)
        }.map(_ => empty_instance)
      } yield instance
    }

  }

  case class ArrayBuilder(children: Seq[InstanceBuilder])
    extends InstanceBuilder {
    override def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RFObject[F]] = {
      lbound match {
        case lb: ArrayType => {
          for {
            empty_instance <- runtime.create_object(id => runtime.constructors.array(lb, id, parent))

            element_instances <- children.traverse(child =>
              child.build(lb.prototype, Some(empty_instance.id), runtime)
            )

            instance <- element_instances.traverse(einst =>
              empty_instance.add_element(einst.id)
            ).map(_ => empty_instance)
          } yield instance
        }
      }
    }
  }

  object Defaults {
    def create(dtype: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RFObject[F]] = {
      dtype match {
        case lb: AtomicDataType =>
          runtime.create_object(id =>
            runtime.constructors.atomic(lb, id, lb.default, parent)
          )
        case lb: RecordType =>
          runtime.create_object(id =>
            runtime.constructors.record(lb, id, parent)
          )

        case lb: UnionType => {
          val concrete: RecordType = lb.subtypes.head
          runtime.create_object(id =>
            runtime.constructors.record(concrete, id, parent)
          )
        }

        case lb: ArrayType =>
          runtime.create_object(id =>
            runtime.constructors.array(lb, id, parent)
          )
      }
    }
  }

  def apply(builder: RecordBuilder): F[RecordObject[F]] =
    builder.build(builder.dtype, None, runtime)


  implicit class Record2Builder(dtype: RecordType.Builder) {
    def apply(children: (String, InstanceBuilder)*): RecordBuilder = {
      RecordBuilder(dtype, children)
    }
  }

  implicit def seq2builder(x: Seq[InstanceBuilder]): ArrayBuilder = ArrayBuilder(x)

//  implicit def value2builder(x: Value): AtomicBuilder = AtomicBuilder(x)
  import Value.implicits._
  implicit def any2atomic[T](x: T)(implicit vc: T => Value): AtomicBuilder = AtomicBuilder(x)
//  implicit def str2ab(x: String): AtomicBuilder = AtomicBuilder(x)
//  implicit def int2ab(x: Int): AtomicBuilder = AtomicBuilder(x)
}
