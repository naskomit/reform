package sysmo.reform.shared.chart

import sysmo.reform.shared.util.{NamedValue, CirceTransport}

object Transport extends CirceTransport {
  import sysmo.reform.shared.query.Transport._
//  import io.circe._
  import io.circe.generic.semiauto.deriveCodec

  implicit val codec_ChartData: Codec[ChartData] = deriveCodec[ChartData]
  implicit val codec_QuerySource: Codec[QuerySource] = deriveCodec[QuerySource]

  implicit val codec_ChartDefinition: Codec[ChartDefinition] = deriveCodec[ChartDefinition]
  implicit val codec_Histogram: Codec[DistributionSettings] = deriveCodec[DistributionSettings]
//  implicit val codec_: Codec[] = deriveCodec[]

  implicit val codec_ChartRequest: Codec[ChartRequest] = deriveCodec[ChartRequest]
  //  implicit val codec_: Codec[] = deriveCodec[]
  //  implicit val codec_: Codec[] = deriveCodec[]


  implicit val codec_ChartObject: Codec[ChartObject] = deriveCodec[ChartObject]
  implicit val codec_Plotly: Codec[Plotly] = deriveCodec[Plotly]
  implicit val codec_ChartResult: Codec[ChartResult] = deriveCodec[ChartResult]
}
