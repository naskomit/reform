package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.coviddata.shared.{data => CD}

object DataDescription {
  val schemas = Seq(
    CD.SocioDemographic.schema,
    CD.Clinical.schema,
    CD.Therapy.schema,
    CD.Immunology.schema
  )
}
