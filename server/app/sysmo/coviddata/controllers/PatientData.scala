package sysmo.coviddata.controllers

import autowire.Server
import upickle.{default => up}
import javax.inject._

import scala.concurrent.ExecutionContext
import ujson.Value
import sysmo.coviddata.shared.data.{PatientData, PatientRecord}

import scala.util.Random

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
    PatientRecord(
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


object PatientDataImpl extends PatientData {
  val total_count = 20
  override def count_patients(): Int = total_count
  override def list_patients(): Seq[PatientRecord] = PatientDataGenerator.generate(total_count)
}

class DataApiServer @Inject()(implicit ec: ExecutionContext) extends Server[Value.Value, up.Reader, up.Writer] {
  override def read[Result: up.Reader](p: Value) = up.read(p)
  override def write[Result: up.Writer](r: Result) = up.writeJs(r)
  val routes = route[PatientData](PatientDataImpl)
}