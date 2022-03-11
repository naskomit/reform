package sysmo.coviddata.controllers

import autowire.Core.Request
import javax.inject._
import autowire.Server
import upickle.{default => up}
import ujson.Value

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import sysmo.coviddata.shared.data.{PatientData, PatientRecord}
import sysmo.reform.db.GraphAppStorage
import sysmo.reform.shared.query.Query

object PatientDataGenerator {
  object pool {
    val first_name = Seq("Иван", "Стоян", "Людмила", "Невена")
    val father_name = Seq("Драганов", "Стоянов", "Стоева", "Петрова", "Милева")
    val last_name = father_name
    val age = Seq(10, 80)
    val gender = Seq("Мъж", "Жена")
    val education =  Seq("основно", "средно", "висше")
//    val contact_name = Seq("Иван Петров")
//    val contact_type = Seq("1")
//    val contact_time = Seq("12.03.22")
//    val social_status = Seq("ss1", "ss2")
//    val has_concomitant_diseases = Seq(true, false)
//    val concomitant_diseases = Seq("Диабет", "Хипертония", "Артрит")
//    val risk_factors = Seq("Имунокомпрометиран", "Високо кръвно", "Възраст")
//    val alcohol = Seq(true, false)
//    val other_risk_1 = Seq("Имунокомпрометиран", "Високо кръвно", "Възраст")
//    val other_risk_2 = Seq("Имунокомпрометиран", "Високо кръвно", "Възраст")

  }

  val random = new Random(42)
  def random_element[T](coll : Seq[T]): T = {
    val index = random.nextInt(coll.length)
    coll(index)
  }

  def random_int(coll : Seq[Int]): Int = {
    random.between(coll(0), coll(1))
  }

  def random_float(coll : Seq[Float]): Float = {
    random.between(coll(0), coll(1))
  }

  def generate_one() : PatientRecord =
    PatientRecord.apply_noid(
      first_name = random_element(pool.first_name),
      father_name = random_element(pool.father_name),
      last_name = random_element(pool.last_name),
      age = random_int(pool.age),
      gender = random_element(pool.gender),
      education = random_element(pool.education),
//      contact_name = random_element(pool.contact_name),
//      contact_type = random_element(pool.contact_type),
//      contact_time = random_element(pool.contact_time),
//      social_status = random_element(pool.social_status),
//      has_concomitant_diseases = random_element(pool.has_concomitant_diseases),
//      concomitant_diseases = random_element(pool.concomitant_diseases),
//      risk_factors = random_element(pool.risk_factors),
//      alcohol = random_element(pool.alcohol),
//      other_risk_1 = random_element(pool.other_risk_1),
//      other_risk_2 = random_element(pool.other_risk_2)
    )

  def generate(n : Int) : Seq[PatientRecord] = {
    for (i <- 0 to n) yield generate_one()
  }
}


//class PatientDataImpl(app_storage: GraphAppStorage) extends PatientData {
//  val total_count = 20
//  override def count_patients(): Int = total_count
//  override def list_patients(): Seq[PatientRecord] = PatientDataGenerator.generate(total_count)
//
//  override def query_gremlin(q: String): Seq[PatientRecord] = {
//    Seq()
//  }
//
//  private def get_single_elem[U](x: java.util.AbstractMap[_, _], k: String) = {
//    type AList = java.util.AbstractList[_]
//    x.get(k).asInstanceOf[AList].get(0).asInstanceOf[U]
//  }
//
//  def query_table(q: Query): Seq[PatientRecord] = {
//    type AMap = java.util.AbstractMap[_, _]
//    val result = app_storage.query_table(q)
//    result.map {
//      case x: AMap =>
//        PatientRecord(
//          None,
//          this.get_single_elem[String](x, "first_name"),
//          this.get_single_elem[String](x, "father_name"),
//          this.get_single_elem[String](x, "last_name"),
//          this.get_single_elem[Int](x, "age"),
//          this.get_single_elem[String](x, "gender"),
//          this.get_single_elem[String](x, "education"),
//        )
//    }
//  }
//}
//
//object PatientDataImpl {
//  def apply(app_storage: GraphAppStorage): PatientDataImpl = new PatientDataImpl(app_storage)
//}
////class DataApiServer @Inject()(implicit ec: ExecutionContext) extends Server[Value.Value, up.Reader, up.Writer] {
////  import sysmo.reform.shared.query.ReadersWriters.rwQuery
//////  override def read[Result: up.Reader](p: Value) = up.read[Value](p)
//////  override def write[Result: up.Writer](r: Result) = up.writeJs(r)
////  val routes = route[PatientData](PatientDataImpl)
////}
//
//class DataApiServer
//  (base_path: Seq[String], app_storage: GraphAppStorage)
//  (implicit ec: ExecutionContext) {
//
//  import sysmo.reform.shared.query.ReadersWriters.rwQuery
//  type PickleType = Value.Value
//  private val route_map = Map(
//    (base_path :+ "count_patients") -> this.count_patients _,
//    (base_path :+ "query_table") -> this.query_table _
//  )
//
//  def count_patients(args: Map[String, PickleType]): PickleType = {
//    val result = PatientDataImpl(app_storage).count_patients()
//    up.writeJs(result)
//
//  }
//
//  def query_table(args: Map[String, PickleType]): PickleType = {
//    val q = upickle.default.read[Query](args("query"))
//    val result = PatientDataImpl(app_storage).query_table(q)
//    up.writeJs(result)
//
//  }
//
//  val routes: autowire.Core.Router[PickleType] = {
//    case Request(path, args) => {
//      val method = route_map.getOrElse(path,
//        throw new IllegalArgumentException(f"Cannot find method for path $path")
//      )
//      Future(method(args))
//    }
//  }
//
//}
