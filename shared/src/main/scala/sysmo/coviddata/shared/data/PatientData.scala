package sysmo.coviddata.shared.data

import upickle.default.{ReadWriter => RW, macroRW}

case class PatientRecord
(
  first_name: String,
  last_name: String
)

object PatientRecord {
  implicit val rw: RW[PatientRecord] = macroRW
}

trait PatientData {
  def list_patients(): Seq[PatientRecord]
}
