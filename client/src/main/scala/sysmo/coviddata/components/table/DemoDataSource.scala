package sysmo.coviddata.components.table

import sysmo.coviddata.components.table.AgGridReact.IGetRowsParams
import scala.scalajs.js

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
  def rowCount : Int = 3
  def getRows(params: IGetRowsParams): Seq[RowData] = {
    Seq(
      RowData("Fender", "Stratocaster", 2019),
      RowData("Gibson", "Les Paul", 1958),
      RowData("Fender", "Telecaster", 1971)
    )
  }

}
