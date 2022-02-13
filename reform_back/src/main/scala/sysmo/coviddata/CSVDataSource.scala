package sysmo.coviddata

import kantan.csv.{CsvConfiguration, HeaderDecoder, HeaderEncoder}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.io.CSVReader

object CSVDataSource {


  implicit val patient_header_decoder : HeaderDecoder[PatientRecord] = HeaderDecoder.decoder(
    "Име",	"Презиме",	"Фамилия",	"Възраст",	"Пол",	"Образование"
  )(PatientRecord.apply_noid)
  implicit val patient_header_encoder : HeaderEncoder[PatientRecord] = HeaderEncoder.caseEncoder("Номер", "Име",	"Презиме",	"Фамилия",	"Възраст",	"Пол",	"Образование")(PatientRecord.unapply)

  def test_read_write_csv() = {
    val out_file_path = "/data/Workspace/SysMo/covid-project/scala/covid-data/doc/SampleData_1___out.csv"
    create_patient_stream().foreach(println)

//    write_file[PatientRecord](content, out_file_path, CsvConfiguration.rfc.withHeader)
//    println(content)

  }

  def create_patient_stream(): Observable[PatientRecord] = {
    val patient_in_file = "/data/Workspace/SysMo/covid-project/scala/covid-data/doc/SampleData_1.csv"
    CSVReader.read[PatientRecord](patient_in_file, CsvConfiguration.rfc.withHeader)
  }

  def read_patient_data(): Seq[PatientRecord] = {
    val patient_in_file = "/data/Workspace/SysMo/covid-project/scala/covid-data/doc/SampleData_1.csv"
    CSVReader.read_seq[PatientRecord](patient_in_file, CsvConfiguration.rfc.withHeader)
  }
}
