package sysmo.coviddata


import monix.eval.Task

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Using}
import slick.jdbc.{JdbcProfile, SQLiteProfile}
import sysmo.reform.db.AppDatabaseStorage
import slick.jdbc.meta.MTable
import sysmo.coviddata.shared.data.PatientRecord

import scala.concurrent.ExecutionContext.Implicits.global

class CovidDatabaseStorage(val jdbc_profile : JdbcProfile = SQLiteProfile)(implicit val ec: ExecutionContext) extends AppDatabaseStorage {
  import jdbc_profile.api._

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

  override val all_tables = Seq(patients)
  //  val table_methods = Seq(patients.delete)
  override val all_schemas = patients.schema
  override val schema_methods = patients.schema
}

object SQLiteAppStorage {
  val db_profile = SQLiteProfile

  def test_import() = {
    val patient_data = CSVDataSource.read_patient_data()

    Using(
      db_profile.api.Database.forURL("jdbc:sqlite:/data/Workspace/SysMo/covid-project/scala/covid-data/doc/db_test1.sqlite")
    ) { db => {
      val app_storage = new CovidDatabaseStorage()
      val initialization =
        app_storage.initialize_schema(db)
        .flatMap(_ => app_storage.empty_tables(db))
        .flatMap(_ => app_storage.insert_batch[PatientRecord](db, app_storage.patients, patient_data))
        .flatMap(_ => app_storage.read_table[PatientRecord](db, app_storage.patients))

      Await.ready(initialization, Duration.Inf)
      initialization.foreach(println)
    }} match {
      case Success(x) => println("Successfully done!")
      case Failure(e) => throw new RuntimeException("Failed", e)
    }

  }
}
