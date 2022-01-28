package sysmo.coviddata.shared.data

import upickle.default.{ReadWriter => RW, macroRW}
import scala.scalajs.js.{annotation => ann}

@ann.JSExportAll
case class PatientRecord
(
  first_name: String,
  father_name: String,
  last_name: String,
  age: Int,
  gender: String,
  education: String,
  contact_name: String,
  contact_type: String,
  contact_time: String,
  social_status: String,
  has_concomitant_diseases: Boolean,
  concomitant_diseases: String,
  risk_factors: String,
  alcohol: Boolean,
  other_risk_1: String,
  other_risk_2: String
)

object PatientRecord {
  implicit val rw: RW[PatientRecord] = macroRW
}

trait PatientData {
  def list_patients(): Seq[PatientRecord]
  def count_patients(): Int
}
