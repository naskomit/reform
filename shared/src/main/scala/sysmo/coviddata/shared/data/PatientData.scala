package sysmo.coviddata.shared.data

import upickle.default.{macroRW, ReadWriter => RW}

import scala.collection.immutable.VectorMap
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
//  contact_name: String,
//  contact_type: String,
//  contact_time: String,
//  social_status: String,
//  has_concomitant_diseases: Boolean,
//  concomitant_diseases: String,
//  risk_factors: String,
//  alcohol: Boolean,
//  other_risk_1: String,
//  other_risk_2: String
)




object PatientRecordMeta extends RecordMeta {
  object FieldEnum extends Enumeration {
    type FieldEnum = Value
    val first_name, father_name, last_name, age, gender, education = Value
//    val father_name = Value("father_name")
//    val last_name = Value("last_name")
//    val age = Value("age")
//    val gender = Value("gender")
//    val education = Value("education")
//    val contact_name = Value("contact_name")
//    val contact_type = Value("contact_type")
//    val contact_time = Value("contact_time")
//    val social_status = Value("social_status")
//    val has_concomitant_diseases = Value("has_concomitant_diseases")
//    val concomitant_diseases = Value("concomitant_diseases")
//    val risk_factors = Value("risk_factors")
//    val alcohol = Value("alcohol")
//    val other_risk_1 = Value("other_risk_1")
//    val other_risk_2 = Value("other_risk_2")
  }
  override type FieldKey = FieldEnum.Value

  val field_keys = Seq(
    FieldEnum.first_name, FieldEnum.father_name, FieldEnum.last_name, FieldEnum.age, FieldEnum.gender, FieldEnum.education
  )

  val fields = VectorMap(
    FieldEnum.first_name -> RecordField(name = "first_name", label = "Име", tpe = StringType()),
    FieldEnum.father_name -> RecordField(name = "father_name", label = "Презиме", tpe = StringType()),
    FieldEnum.last_name -> RecordField(name = "last_name", label = "Фамилия", tpe = StringType()),
    FieldEnum.age -> RecordField(name = "age", label = "Възраст", tpe = StringType()),
    FieldEnum.gender -> RecordField(name = "gender", label = "Пол", tpe = StringType()),
    FieldEnum.education -> RecordField(name = "education", label = "Образование", tpe = StringType()),
  )
}

object PatientRecord {
  implicit val rw: RW[PatientRecord] = macroRW
  implicit val tometa :  RecordWithMeta[PatientRecord] = new RecordWithMeta[PatientRecord] {
    val _meta: RecordMeta = PatientRecordMeta
  }
}

trait PatientData {
  def list_patients(): Seq[PatientRecord]
  def count_patients(): Int
}
