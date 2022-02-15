package sysmo.coviddata


import monix.eval.Task

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Using}
import slick.jdbc.{JdbcProfile, SQLiteProfile}
import sysmo.reform.db.{AppDatabaseStorage, DBConfiguration}
import sysmo.coviddata.shared.data.PatientRecord

//import ExecutionContext.Implicits.global
import monix.execution.Scheduler.Implicits.global

class CovidDatabaseStorage(override val db_config: DBConfiguration
  )(implicit val ec: ExecutionContext) extends AppDatabaseStorage {
  import db_config.jdbc_profile.api._

//  type PatientRecordTableType = (Int, String, String, String, Int, String, String)
  class PatientRecordTable(tag: Tag) extends Table[PatientRecord](tag, "patient_records") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def first_name = column[String]("first_name")
    def father_name = column[String]("father_name")
    def last_name = column[String]("last_name")
    def age = column[Int]("age")
    def gender = column[String]("gender")
    def education = column[String]("education")
    override def * = (id.?, first_name, father_name, last_name, age, gender, education) <>
      ((PatientRecord.apply _).tupled, PatientRecord.unapply)
  }
  val patients = TableQuery[PatientRecordTable]

  override def all_tables = Seq(patients)
}

object SQLiteAppStorage {
  val db_conf = "jdbc:sqlite:/data/Workspace/SysMo/covid-project/scala/covid-data/doc/db_test1.sqlite"
  val app_storage = new CovidDatabaseStorage(
    new DBConfiguration {
      override val jdbc_profile = SQLiteProfile
      def connection : jdbc_profile.api.Database = {
        val conn = jdbc_profile.api.Database.forConfig("sqlite")
//        val conn = jdbc_profile.api.Database.forURL(db_conf)
        conn
      }
  })

  def test_import() = {
    println("Test import")
    val patient_data = CSVDataSource.read_patient_data()
//    Using(app_storage.db_config.connection) { db =>
      val initialization = app_storage.initialize_schema
        .flatMap(_ => app_storage.empty_tables)
//        .flatMap(_ => app_storage.insert_batch[PatientRecord](app_storage.patients, patient_data))
      //        .flatMap(_ => app_storage.read_table[PatientRecord](db, app_storage.patients))

      initialization.runSyncUnsafe(Duration("2s"))

//    }
  }
//
//  def test_query() = {
//    Using(db_profile.api.Database.forURL(db_conf)) { db => {
//      println("List")
//      val app_storage
//
//    }}
//  }

}
