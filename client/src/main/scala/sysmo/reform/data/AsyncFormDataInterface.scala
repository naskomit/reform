package sysmo.reform.data

import sysmo.coviddata.shared.data.PatientRecord

import scala.concurrent.Future

trait AsyncFormDataInterface[U] {
  def get_record(id: String): Future[U]
  def update_record(id: String, value: U): Future[String]
}


