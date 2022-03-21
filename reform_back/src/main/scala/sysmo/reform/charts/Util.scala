package sysmo.reform.charts

import sysmo.reform.shared.data.{table => sdt}

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
}
