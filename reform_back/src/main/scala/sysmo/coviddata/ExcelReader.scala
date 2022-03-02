package sysmo.coviddata

import org.apache.poi.ss.usermodel.CellType

import scala.util.Using
import scala.jdk.CollectionConverters._

class ExcelReader {

}

object ExcelReader {
  def test1(): Unit = {
    val doc_path = "doc/SampleData_2_RandomData.xlsx"
//    import org.apache.poi.ss.usermodel.Workbook
    import org.apache.poi.xssf.usermodel.XSSFWorkbook
    import java.io.FileInputStream
    Using(new FileInputStream(doc_path)) (fs => {
      Using(new XSSFWorkbook(fs)) (workbook => {
        val sheet = workbook.getSheetAt(0)
        val row = sheet.getRow(4)
        for (cell <- row.asScala) {
          cell.getCellType match {
            case CellType.STRING => println(cell.getStringCellValue)
            case CellType.NUMERIC => println(cell.getNumericCellValue)
            case CellType.BLANK =>
            case x => println(f"Other field type found $x")
          }
        }
      }).get
    }).get
  }
}