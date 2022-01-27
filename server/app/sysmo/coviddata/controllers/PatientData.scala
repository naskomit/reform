package sysmo.coviddata.controllers

import autowire.Server
import upickle.{default => up}
import javax.inject._
import scala.concurrent.ExecutionContext
import ujson.{Value}
import sysmo.coviddata.shared.data.{PatientData, PatientRecord}

object PatientDataImpl extends PatientData {
  override def list_patients(): Seq[PatientRecord] = {
    Seq(
      PatientRecord("John", "Peterson"),
      PatientRecord("Ivan", "Petrov")
    )
  }
}

class DataApiServer @Inject()(implicit ec: ExecutionContext) extends Server[Value.Value, up.Reader, up.Writer] {
  override def read[Result: up.Reader](p: Value) = up.read(p)
  override def write[Result: up.Writer](r: Result) = up.writeJs(r)
  val routes = route[PatientData](PatientDataImpl)
}