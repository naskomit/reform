package sysmo.coviddata
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import slick.jdbc.SQLiteProfile
import sysmo.reform.db.{RelationalDatabaseStorage, DBConfiguration}
import sysmo.coviddata.shared.data.PatientRecord
import sysmo.reform.shared.{query => Q}


class CovidDatabaseStorage(override val db_config: DBConfiguration
  )(implicit val ec: ExecutionContext) extends RelationalDatabaseStorage {
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

    val __table_name = "PatientRecordTable"
    def _num_column(x: String): Rep[Int] = x match {
      case "id" => this.id
      case "age" => this.age
      case _ => throw new IllegalArgumentException(f"No numerical column $x in ${__table_name}")
    }

    def _char_column(x: String): Rep[String] = x match {
      case "first_name" => this.first_name
      case "father_name" => this.father_name
      case "gender" => this.gender
      case "education" =>this.education
      case _ => throw new IllegalArgumentException(f"No char column $x in ${__table_name}")
    }
  }

  object PatientRecordTable {

  }

  val patients = TableQuery[PatientRecordTable]
  override def all_tables = Seq(patients)

  def query_patient_records(q: Q.BasicQuery): Seq[PatientRecord] = {
    val Q.BasicQuery(source, columns, filter, sort, range) = q

    val slick_query123 = patients.filter(x => {
      x.age > 10 && x.gender == "мъж"
    })

    println(patients.shaped)
    def slick_query = Some(patients.filter(_._num_column("age") > 10)).map(x => {
      filter match {
        case Some(flt) => x //.filter(use_query_filter(_, flt))
        case None => x
      }
    }).get
//      patients
//    else
//      patients.filter(_.age > 5)
//      ((x : Query[U, _, _]) => {
//        filter match {
//          case Some(flt) => x
//          case None => x
//        }
//      })(patients)
//      compose
//      ((x : Query[PatientRecordTable, PatientRecord, Seq]) => {
//        x
//      })

//    val slick_query = patients.filter(x => use_query_filter(x, filter))
    Await.result(query(slick_query).runToFuture, 1.second)

  }

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

//  import app_storage.db_config.jdbc_profile.api._

  def announce(x: String) = println(f"==================== $x ====================")

  def test_import() = {
    announce("Test import")
    val patient_data = CSVDataSource.read_patient_data()

    //    println(app_storage.patients.result.statements)
    //    println(app_storage.patients.filter(x => x.age > 10 || x.age < 3).result.statements)
    //    println(app_storage.patients.schema.create.statements)

    val initialization = app_storage.initialize_schema
      .flatMap(_ => app_storage.empty_tables)
      .flatMap(_ => app_storage.insert_batch[PatientRecord](app_storage.patients, patient_data))
      .flatMap(_ => app_storage.query(app_storage.patients))
      .foreachL(println)

    initialization.runSyncUnsafe(Duration("2s"))
  }

  def test_query() = {
    announce("Test query")
    val q = Q.BasicQuery(
      source = Q.SingleTable("PatientData"),
      filter = Some(Q.QueryFilter(
        Q.NumericalPredicate(Q.NumericalPredicateOp.>=, Q.ColumnRef("age"), Q.Val(10)))
      ),
      sort = Some(Q.QuerySort()),
      range = Some(Q.QueryRange(3, 4))
    )

    val res = app_storage.query_patient_records(q)
    println(res)
  }


}
