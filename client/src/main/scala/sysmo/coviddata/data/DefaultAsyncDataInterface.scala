package sysmo.coviddata.data

import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.data.AsyncFormDataInterface

import scala.concurrent.Future

object DefaultAsyncDataInterface extends AsyncFormDataInterface[PatientRecord] {
  override def get_record(id: String) = Future.successful(
    PatientRecord.apply_noid(
      first_name = "Джон", father_name = "И", last_name = "Атанасов",
      age = 63, gender = "мъж", education = "висше"
    )
  )
  override def update_record(id: String, value: PatientRecord) = Future.successful(
    "Record updated"
  )

}
