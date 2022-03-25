package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{EnumeratedDomain, EnumeratedOption, IntegerType, Record, RecordField, RecordMeta, RecordWithMeta, StringType}
import sysmo.reform.shared.query.Query
import upickle.default.{macroRW, ReadWriter => RW}

import scala.collection.immutable.VectorMap
import scala.scalajs.js.{annotation => ann}




@ann.JSExportAll
case class PatientRecord
( id: Option[Int],
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
) extends Record

//
//case class Pathology(
//  id: String,
//  `Лаб. №`: String,
//  `PCR от назофар. секрет`: String,
//  `MIP2-ELISA`: String,
//  `TNF-α -ELISA`: String,
//  `TGF-β1- ELISA`: String,
//  `Ang(1–7)-ELISA`: String,
//  `ACE2- ELISA`: String,
//  `chatepsins L-IH`: String,
//  `ACE2-IH:стойност`: String,
//  `TGF-β1- IH`: String,
//  `p-38-IH`: String,
//  `IL-1β-IH`: String,
//  `eNOS-IH`: String,
//  `iNOS-IH`: String,
//  `Придружаващи заболявания`: String,
//)
//
////object Pathology {
////  def f1 = new Pathology(`iNOS-IH`= "asda")
////}
//
//
//object PatientRecordMeta extends RecordMeta[PatientRecord] {
//  val id = "PatientRecord"
//  object FieldEnum extends Enumeration {
//    type FieldEnum = Value
////    val first_name, father_name, last_name, age, gender, education = Value
//    val first_name = Value("first_name")
//    val father_name = Value("father_name")
//    val last_name = Value("last_name")
//    val age = Value("age")
//    val gender = Value("gender")
//    val education = Value("education")
////    val contact_name = Value("contact_name")
////    val contact_type = Value("contact_type")
////    val contact_time = Value("contact_time")
////    val social_status = Value("social_status")
////    val has_concomitant_diseases = Value("has_concomitant_diseases")
////    val concomitant_diseases = Value("concomitant_diseases")
////    val risk_factors = Value("risk_factors")
////    val alcohol = Value("alcohol")
////    val other_risk_1 = Value("other_risk_1")
////    val other_risk_2 = Value("other_risk_2")
////    def fromString(s: String): Option[Value] = values.find(_.toString == s)
//  }
//  override type FieldKey = FieldEnum.Value
//
//  val field_keys = Seq(
//    FieldEnum.first_name, FieldEnum.father_name, FieldEnum.last_name, FieldEnum.age, FieldEnum.gender, FieldEnum.education
//  )
//
//  val fields = VectorMap(
//    FieldEnum.first_name -> RecordField(name = "first_name", label = "Име", tpe = StringType()),
//    FieldEnum.father_name -> RecordField(name = "father_name", label = "Презиме", tpe = StringType()),
//    FieldEnum.last_name -> RecordField(name = "last_name", label = "Фамилия", tpe = StringType()),
//    FieldEnum.age -> RecordField(name = "age", label = "Възраст", tpe = IntegerType()),
//    FieldEnum.gender -> RecordField(name = "gender", label = "Пол", tpe = StringType(),
//      //domain = Some(EnumeratedDomain(Seq("male", "female")))
//      domain = Some(EnumeratedDomain(Seq(EnumeratedOption("male", "мъж"), EnumeratedOption("female", "жена"))))
//    ),
//    FieldEnum.education -> RecordField(name = "education", label = "Образование", tpe = StringType()),
//  )
//
//  def field_key(name : String): FieldKey = FieldEnum.withName(name)
//
//
//
//  override def get_value(obj : PatientRecord, key : FieldKey): Any = {
//    key match {
//      case FieldEnum.first_name => obj.first_name
//      case FieldEnum.father_name => obj.father_name
//      case FieldEnum.last_name => obj.last_name
//      case FieldEnum.age => obj.age
//      case FieldEnum.gender => obj.gender
//      case FieldEnum.education => obj.education
//    }
//
//  }
//
//  override def update_value(obj : PatientRecord, key : FieldKey, value : Any): PatientRecord = {
//    key match {
//      case FieldEnum.first_name => obj.copy(first_name = check_field_type[String](key, value))
//      case FieldEnum.father_name => obj.copy(father_name = check_field_type[String](key, value))
//      case FieldEnum.last_name => obj.copy(last_name = check_field_type[String](key, value))
//      case FieldEnum.age => obj.copy(age = check_field_type[Int](key, value))
//      case FieldEnum.gender => obj.copy(gender = check_field_type[String](key, value))
//      case FieldEnum.education => obj.copy(education = check_field_type[String](key, value))
//    }
//
//  }
//}
//
object PatientRecord {
  def apply_noid(first_name: String, father_name: String, last_name: String, age: Int, gender: String, education: String): PatientRecord =
    new PatientRecord(None, first_name, father_name, last_name, age, gender, education)
  implicit val rw: RW[PatientRecord] = macroRW
//  implicit val tometa :  RecordWithMeta[PatientRecord] = new RecordWithMeta[PatientRecord] {
//    val _meta: RecordMeta[PatientRecord] = PatientRecordMeta
//  }
}

trait PatientData {
  def list_patients(): Seq[PatientRecord]
  def count_patients(): Int
  def query_gremlin(q: String): Seq[PatientRecord]
  def query_table(q: Query): Seq[PatientRecord]
}
