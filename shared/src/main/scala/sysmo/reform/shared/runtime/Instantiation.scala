package sysmo.reform.shared.runtime

import cats.MonadThrow
import cats.implicits._
import sysmo.reform.shared.data.{ObjectId, Value}
import sysmo.reform.shared.runtime.LocalRuntime.AtomicObjectImpl
import sysmo.reform.shared.types.{AtomicDataType, DataType, RecordType, UnionType}

class Instantiation[F[+_]](runtime: ObjectRuntime[F]) {
  implicit val mt: MonadThrow[F] = runtime.mt
  trait InstanceBuilder {
    def build(lbound: DataType, parent: Option[ObjectId], runtime: ObjectRuntime[F]): F[RuntimeObject[F]]
  }

  case class AtomicBuilder(value: Value)
    extends InstanceBuilder {
    override def build(lbound: DataType, parent: Option[ObjectId], runtime: ObjectRuntime[F]): F[RuntimeObject[F]] = {
      lbound match {
        case lb: AtomicDataType => runtime.create_object(id => runtime.constructors.atomic(lb, id, value, parent))
        case _ => runtime.mt.raiseError(new IllegalArgumentException("Lower bound for AtomicObject should be of type AtomicDataType"))
      }
    }
  }

  case class RecordBuilder(dtype: RecordType, children: Seq[(String, InstanceBuilder)])
    extends InstanceBuilder {
    override def build(lbound: DataType, parent: Option[RuntimeObject[F]], runtime: ObjectRuntime[F]): F[RuntimeObject[F]] = {
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

      val child_value_map = children.toMap
      for {
        instance <- runtime.create_object(id => runtime.constructors.record(dtype, id, parent.map(_.id)))
        _ <- {
          val field_relations = dtype.fields
          val illegal_keys = child_value_map.keys.toSet.diff(field_relations.map(_.name).toSet)
          if (illegal_keys.nonEmpty) {
            return mt.raiseError(new IllegalArgumentException(
              s"No such fields $illegal_keys in group ${dtype.symbol}"
            ))
          }
          //field_relations.traverse()

          field_relations.foreach { rel =>
            val child_instance: RuntimeObject = (child_value_map.get(rel.name) match {
              case Some(v) => v.build(rel.dtype, instance, runtime)
              case None => Defaults.create(rel.dtype, instance)
            })
            instance.children.addOne(rel.name -> child_instance.id)
          }
        }
      } yield instance
    }

  }

  case class ArrayBuilder(children: Seq[InstanceBuilder])
    extends InstanceBuilder {
    override def build(lbound: DataType, parent: Option[RuntimeObject[F]], runtime: ObjectRuntime[F]): RuntimeObject[F] = {
      ???
    }
  }

  object Defaults {
    def create(dtype: DataType, parent: Option[ObjectId], runtime: ObjectRuntime[F]): F[RuntimeObject[F]] = {
      dtype match {
        case lb: AtomicDataType => runtime.create_object(id => runtime.constructors.atomic(lb, id, lb.default, parent))
      }
    }
  }


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
