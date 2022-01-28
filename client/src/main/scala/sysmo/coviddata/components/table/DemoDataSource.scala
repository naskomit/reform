package sysmo.coviddata.components.table

import sysmo.coviddata.components.table.AgGridFacades.IGetRowsParams
import scala.scalajs.js
import scala.concurrent.Future

trait RowData extends js.Object {
  val make: String
  val model: String
  val year: Int
}

object RowData {
  def apply(make: String, model: String, year: Int): RowData =
    js.Dynamic.literal("make" -> make, "model" -> model, "year" -> year).asInstanceOf[RowData]
}



object DemoDataSource extends TableDatasource[RowData] {
  def rowCount : Future[Int] = Future.successful(3)
  def getRows(params: IGetRowsParams): RemoteBatch = {
    Future.successful(
      Seq(
        RowData("Fender", "Stratocaster", 2019),
        RowData("Gibson", "Les Paul", 1958),
        RowData("Fender", "Telecaster", 1971)
      )
    )
  }

}
