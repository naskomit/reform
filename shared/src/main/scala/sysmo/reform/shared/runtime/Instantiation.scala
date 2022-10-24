package sysmo.reform.shared.runtime

import cats.MonadThrow
import cats.implicits._
import sysmo.reform.shared.data.{ObjectId, Value, ValueConstructor}
import sysmo.reform.shared.types.{ArrayType, CompoundDataType, DataType, MultiReferenceType, PrimitiveDataType, RecordType, ReferenceType, UnionType}

class Instantiation[F[+_]](runtime: RFRuntime[F]) {
  import Value.implicits._
  implicit val mt: MonadThrow[F] = runtime.mt

  sealed trait Builder

  trait ValueBuilder extends Builder {
    def value: Value
  }

  trait InstanceBuilder extends Builder {
    def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RFObject[F]]
  }

  case class RecordBuilder(dtype: RecordType, children: Seq[(String, Builder)])
    extends InstanceBuilder {

    override def build(lbound: DataType, parent: Option[ObjectId], runtime: RFRuntime[F]): F[RecordInstance[F]] = {
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

        instance <- {
          val field_instances_f = dtype.fields.traverse { field =>
            val child_instance = child_map.get(field.name) match {
              case Some(ib: InstanceBuilder) => {
                val child_f = ib.build(field.dtype, Some(empty_instance.id), runtime)
                mt.flatMap(child_f)(child => empty_instance.set_field(field.name, Value(child.id)))
              }
              case Some(vb: ValueBuilder) =>
                empty_instance.set_field(field.name, vb.value)
              case None => {
                field.dtype match {
                  case dt: CompoundDataType => {
                    val child_f = Defaults.create(dt, Some(empty_instance.id), runtime)
                    mt.flatMap(child_f)(child => empty_instance.set_field(field.name, Value(child.id)))
                  }
                  case dt: ArrayType => {
                    val child_f = Defaults.create(dt, Some(empty_instance.id), runtime)
                    mt.flatMap(child_f)(child => empty_instance.set_field(field.name, Value(child.id)))

                  }
                  case _ => mt.pure()
                }
              }
            }
            child_instance
          }

          field_instances_f.map(_ => empty_instance)
        }

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

  def apply(builder: RecordBuilder): F[RecordInstance[F]] =
    builder.build(builder.dtype, None, runtime)


  implicit class Record2Builder(dtype: RecordType.Builder) {
    def apply(children: (String, Builder)*): RecordBuilder = {
      RecordBuilder(dtype, children)
    }
  }

  implicit class ValueBuilderImpl[T](raw: T)(implicit vc: ValueConstructor[T] ) extends ValueBuilder {
    override def value: Value = vc.toValue(raw)
  }

  implicit class Seq2Builder(x: Seq[InstanceBuilder]) extends ArrayBuilder(x)
}
