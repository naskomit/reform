package sysmo.coviddata.components.table

import sysmo.coviddata.shared.data.{PatientData, PatientRecord}
import sysmo.coviddata.services.DataApiClient
import autowire._
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

object DemoServerDataSource extends TableDatasource[PatientRecord]{
  override def rowCount: Future[Int] = {
    DataApiClient[PatientData].count_patients().call()
  }

  override def getRows(params: AgGridFacades.IGetRowsParams): RemoteBatch = {
    DataApiClient[PatientData].list_patients().call()
  }
}
