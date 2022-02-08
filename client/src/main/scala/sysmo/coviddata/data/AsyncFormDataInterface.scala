package sysmo.coviddata.data

import sysmo.coviddata.shared.data.PatientRecord

import scala.concurrent.Future

trait AsyncFormDataInterface[U] {
  def get_record(id: String): Future[U]
  def update_record(id: String, value: U): Future[String]
}

object DefaultAsyncDataInterface extends AsyncFormDataInterface[PatientRecord] {
  override def get_record(id: String) = Future.successful(
    PatientRecord(
      first_name = "Джон", father_name = "И", last_name = "Атанасов",
      age = 63, gender = "мъж", education = "висше"
    )
  )
  override def update_record(id: String, value: PatientRecord) = Future.successful(
    "Record updated"
  )

}
