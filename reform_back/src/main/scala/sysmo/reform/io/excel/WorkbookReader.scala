package sysmo.reform.io.excel

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFSheet, XSSFWorkbook}
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.shared.util.pprint
import sysmo.reform.util.Logging

import scala.collection.mutable
import scala.util.Using
import scala.jdk.CollectionConverters._

case class Column(index: Int)
case class Row(index: Int)

sealed trait Action

trait Move extends Action
case class Up(n: Int = 1) extends Move
case class Down(n: Int = 1) extends Move
case class Left(n: Int = 1) extends Move
case class Right(n: Int = 1) extends Move
object Right {
  val _1 = Right()
  val _2 = Right(n = 2)
}
//case object Right1 extends Right(1)
//case object Right2 extends Right(2)

case class PositionAt(row: Int, col: Int) extends Move

case class Read(field_id: String) extends Action
case class ReadRow(actions: Seq[Action]) extends Action
object ReadRow {
  class Builder(schema: sdt.Schema) {
    private var actions = mutable.ArrayBuffer[Action]()
    def read(field_id: String): Builder = {
      actions += Read(field_id)
      this
    }
    def shift_read(field_id: String): Builder = {
      actions ++= Seq(Right(), Read(field_id))
      this
    }
    def shift_2_read(field_id: String): Builder = {
      actions ++= Seq(Right(2), Read(field_id))
      this
    }
    def shift_n_read(n: Int, field_id: String): Builder = {
      actions ++= Seq(Right(n), Read(field_id))
      this
    }

    def build: ReadRow = ReadRow(actions.toSeq)
  }

  def builder(schema: sdt.Schema): Builder  = new Builder(schema)
}

case class ReadBlock(schema: sdt.Schema, worksheet: String, start: PositionAt, read_row: ReadRow)

case class TableCollectionRead(prog: Map[String, ReadBlock])


class WorkbookReader(file_path: String, table_manager: sdt.TableManager) extends Logging {
  private val _current_sheet = new scala.util.DynamicVariable[Option[XSSFSheet]](None)
  def current_sheet: XSSFSheet = _current_sheet.value match {
    case Some(sheet) => sheet
    case None => throw new IllegalStateException("No current sheet!")
  }

  def parse_value(cell: XSSFCell, cell_type: CellType, field_type: sdt.VectorType.Value): sdt.Value = {
    cell_type match {
      case CellType.STRING => {
        val content = cell.getStringCellValue
        field_type match {
          case sdt.VectorType.Char => sdt.Value(Some(content), field_type)
          case sdt.VectorType.Bool => sdt.Value(None, field_type)
          case sdt.VectorType.Int => sdt.Value(content.toIntOption, field_type)
          case sdt.VectorType.Real => sdt.Value(content.toDoubleOption, field_type)
        }
      }
      case CellType.NUMERIC => {
        val content = cell.getNumericCellValue
        field_type match {
          case sdt.VectorType.Char => sdt.Value(Some(content.toString), field_type)
          case sdt.VectorType.Bool => sdt.Value(None, field_type)
          case sdt.VectorType.Int => sdt.Value(Some(content.round), field_type)
          case sdt.VectorType.Real => sdt.Value(Some(content), field_type)
        }
      }
//      case CellType.BLANK => {
//        sdt.Value(None, field_type)
//      }
      case x => {
        logger.warn(f"Other field type found $x")
        sdt.Value(None, field_type)
      }
    }
  }


  def read_value(row: Int, col: Int, field_def: sdt.Field): sdt.Value = {
    val cell = current_sheet.getRow(row).getCell(col)
    val field_type = field_def.field_type.tpe
    if (cell == null)
      return sdt.Value(None, field_type)
    val cell_type = cell.getCellType
    cell_type match {
      case CellType.FORMULA => parse_value(cell, cell.getCachedFormulaResultType, field_type)
      case CellType.BLANK => sdt.Value(None, field_type)
      case _ => parse_value(cell, cell_type, field_type)
    }
  }

  def read_row(prog: ReadRow, schema: sdt.Schema, start: PositionAt): Map[String, sdt.Value] = {
    var col = start.col
    prog.actions.foldLeft(Map[String, sdt.Value]()) {(acc, action) => {
      action match {
        case Left(n) => {col -= n; acc}
        case Right(n) => {col += n; acc}
        case Read(field_id) => {
          schema.field(field_id) match {
            case Some(f) => acc + (field_id -> read_value(start.row, col, f))
            case None => throw new IllegalArgumentException(f"No field $field_id present in the schema")
          }


        }
      }
    }}
  }

  def read_block(prog: ReadBlock, workbook: XSSFWorkbook): sdt.Table = {
    val sheet = workbook.getSheet(prog.worksheet)
    _current_sheet.withValue(Some(sheet)) {
      val corner = prog.start
      val builder = table_manager.incremental_table_builder(prog.schema)
      var row_index = prog.start.row
      for (row <- sheet.rowIterator().asScala) {
        if (row.getRowNum >= row_index) {
          val values = read_row(prog.read_row, prog.schema, prog.start.copy(row = row_index))
          builder.append_value_map(values)
          row_index += 1
        }
      }
      builder.toTable
    }
  }

  def read_table_collection(tc: TableCollectionRead): Map[String, sdt.Table] = {
    Using(new FileInputStream(file_path)) (fs => {
      Using(new XSSFWorkbook(fs)) (workbook => {
        tc.prog.map {case (table_name, block_prog : ReadBlock) => {
          logger.info(f"Reading table '$table_name' from sheet '${block_prog.worksheet}'")
          (table_name, read_block(block_prog, workbook))
        }}.toMap

      }).get
    }).get

  }

}


