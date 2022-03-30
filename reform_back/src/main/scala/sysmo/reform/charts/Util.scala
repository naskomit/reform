package sysmo.reform.charts

import sysmo.reform.shared.data.{table => sdt}

import scala.reflect.{ClassTag, classTag}

object Util {
  def plotly_series(x: sdt.Series): plotly.Sequence = {
    val values = x
      .filter(element => element.is_set)

    x.field.field_type.tpe match {
      case sdt.VectorType.Real => plotly.Sequence.Doubles(values.map(_.as_real.get).toSeq)
      case sdt.VectorType.Int => plotly.Sequence.Doubles(values.map(_.as_real.get).toSeq)
      case sdt.VectorType.Bool => plotly.Sequence.Doubles(values.map(_.as_real.get).toSeq)
      case sdt.VectorType.Char => plotly.Sequence.Strings(values.map(_.as_char.get).toSeq)
    }
  }

  def plotly_series[A](seq: Seq[A])(implicit tag: ClassTag[A]): plotly.Sequence = {
    if (seq.isEmpty) {
      return plotly.Sequence.Doubles(Seq())
    }
    seq match {
      case x: Seq[Double @unchecked] if tag == classTag[Double] => plotly.Sequence.Doubles(x)
      case x: Seq[Int @unchecked] if tag == classTag[Int] => plotly.Sequence.Doubles(x.asInstanceOf[Seq[Int]].map(v => v.toDouble))
      case x: Seq[Long @unchecked] if tag == classTag[Long] => plotly.Sequence.Doubles(x.asInstanceOf[Seq[Long]].map(v => v.toDouble))
      case x: Seq[Boolean @unchecked] if tag == classTag[Boolean] => plotly.Sequence.Strings(x.asInstanceOf[Seq[Boolean]].map(v => v.toString))
      case x: Seq[String @unchecked] if tag == classTag[String]  => plotly.Sequence.Strings(x)
      case x => throw new IllegalArgumentException(s"Cannot convert sequence of type ${tag.runtimeClass} to plotly series ")
    }
  }
}
