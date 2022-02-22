package sysmo.coviddata.data

import autowire._
import sysmo.reform.services.DataApiClient
import sysmo.coviddata.shared.data.{PatientData, PatientRecord}
import sysmo.reform.data.TableDatasource
import sysmo.reform.shared.query.Query

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object DemoServerDataSource extends TableDatasource[PatientRecord]{
  override def row_count: Future[Int] = {
    DataApiClient[PatientData].count_patients().call()
  }

  override def run_query(q : Query): RemoteBatch = {
    DataApiClient[PatientData].list_patients().call()
  }
}
