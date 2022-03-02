package sysmo.reform.data.table

import java.nio.charset.StandardCharsets

import sysmo.reform.shared.data.{table => sdt}
import org.apache.arrow.{vector => av} //{VarCharVector, IntVector, VectorSchemaRoot, FieldVector}

class ArrowIntVector(storage: av.IntVector) extends sdt.IntVector {
  def apply(i: Int): ValueType = storage.get(i)
  override def length = storage.getValueCount
  override def range(start: Int, length: Int): VecT = {
    val tp = storage.getTransferPair(storage.getAllocator)
    tp.splitAndTransfer(start, length)
    new ArrowIntVector(tp.getTo.asInstanceOf[av.IntVector])
  }
  override def close(): Unit = storage.close()
}

class ArrowRealVector(storage: av.Float8Vector) extends sdt.RealVector {
  def apply(i: Int): ValueType = storage.get(i)
  def length = storage.getValueCount
  override def range(start: Int, length: Int): VecT = {
    val tp = storage.getTransferPair(storage.getAllocator)
    tp.splitAndTransfer(start, length)
    new ArrowRealVector(tp.getTo.asInstanceOf[av.Float8Vector])
  }
  override def close(): Unit = storage.close()
}

class ArrowBoolVector(storage: av.BitVector) extends sdt.BoolVector {
  def apply(i: Int): ValueType = storage.get(i) == 1
  def length = storage.getValueCount
  override def range(start: Int, length: Int): VecT = {
    val tp = storage.getTransferPair(storage.getAllocator)
    tp.splitAndTransfer(start, length)
    new ArrowBoolVector(tp.getTo.asInstanceOf[av.BitVector])
  }
  override def close(): Unit = storage.close()
}

class ArrowCharVector(storage: av.VarCharVector) extends sdt.CharVector {
  def apply(i: Int): ValueType = new String(storage.get(i), StandardCharsets.UTF_8)
  def length = storage.getValueCount
  override def range(start: Int, length: Int): VecT = {
    val tp = storage.getTransferPair(storage.getAllocator)
    tp.splitAndTransfer(start, length)
    new ArrowCharVector(tp.getTo.asInstanceOf[av.VarCharVector])
  }
  override def close(): Unit = storage.close()
}

