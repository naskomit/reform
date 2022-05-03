package sysmo.reform.io.excel

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.{CellType, DateUtil}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFSheet, XSSFWorkbook}
import sysmo.reform.shared.data.{table => sdt}
import sysmo.reform.shared.util.pprint
import sysmo.reform.util.Logging

import scala.collection.mutable
import scala.util.Using
import scala.jdk.CollectionConverters._

sealed trait Action

sealed trait Move extends Action
case class Up(n: Int = 1) extends Move
case class Down(n: Int = 1) extends Move
case class Left(n: Int = 1) extends Move
case class Right(n: Int = 1) extends Move
//object Right {
//  val _1 = Right()
//  val _2 = Right(n = 2)
//}
case class Column(index: Int) extends Move
case class Row(index: Int) extends Move

//case object Right1 extends Right(1)
//case object Right2 extends Right(2)
//case class Column(index: Int) extends Move
//case class Row(index: Int) extends Move
case class PositionAt(row: Int, col: Int) extends Move

case class Read(field_id: String) extends Action
case class AssertValue(value: sdt.Value[_], tpe: sdt.FieldType) extends Action
case class CheckValue(check: sdt.Value[_] => Boolean, tpe: sdt.FieldType) extends Action

sealed trait ReadRow
case class ReadSimpleRow(actions: Seq[Action]) extends ReadRow
case class ReadMultiColumnBlockRow(fixed: ReadSimpleRow,
                                   n_max: Int,
                                   variable: ReadSimpleRow,
                                   drop_empty_blocks: Boolean = true) extends ReadRow

object ReadRow {
  class Builder(schema: sdt.Schema) {
    private var actions = mutable.ArrayBuffer[Action]()
    def read(field_id: String): Builder = {
      actions += Read(field_id)
      this
    }
    def col(index: Int): Builder = {
      actions += Column(index)
      this
    }
    def col(address: String): Builder = {
      val ref = new org.apache.poi.ss.util.CellReference(address + "1")
      actions += Column(ref.getCol)
      this
    }
    def shift(n: Int = 1): Builder = {
      actions += Right(n)
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

    def assert_value(v: Double): Builder = {
      actions += AssertValue(
        sdt.RealValue(Some(v)), sdt.FieldType(sdt.VectorType.Real)
      )
      this
    }

    def assert_value(v: Int): Builder = {
      actions += AssertValue(
        sdt.IntValue(Some(v)), sdt.FieldType(sdt.VectorType.Int)
      )
      this
    }

    def assert_value(v: Boolean): Builder = {
      actions += AssertValue(
        sdt.BoolValue(Some(v)), sdt.FieldType(sdt.VectorType.Bool)
      )
      this
    }

    def assert_value(v: String): Builder = {
      actions += AssertValue(
        sdt.CharValue(Some(v)), sdt.FieldType(sdt.VectorType.Char)
      )
      this
    }

    def check_real_value(check: sdt.Value[_] => Boolean): Builder = {
      actions += CheckValue(
        check, sdt.FieldType(sdt.VectorType.Real)
      )
      this
    }

    def check_int_value(check: sdt.Value[_] => Boolean): Builder = {
      actions += CheckValue(
        check, sdt.FieldType(sdt.VectorType.Int)
      )
      this
    }
    def check_bool_value(check: sdt.Value[_] => Boolean): Builder = {
      actions += CheckValue(
        check, sdt.FieldType(sdt.VectorType.Bool)
      )
      this
    }
    def check_char_value(check: sdt.Value[_] => Boolean): Builder = {
      actions += CheckValue(
        check, sdt.FieldType(sdt.VectorType.Char)
      )
      this
    }

    def build: ReadSimpleRow = ReadSimpleRow(actions.toSeq)
  }

  class MultiColumnBlockBuilder(schema: sdt.Schema) {
    val fixed_builder = new Builder(schema)
    val variable_builder = new Builder(schema)
    var _n_max: Int = -1

    def fixed(f: Builder => Unit): this.type = {
      f(fixed_builder)
      this
    }
    def variable(n_max: Int, f: Builder => Unit): this.type = {
      f(variable_builder)
      _n_max = n_max
      this
    }
    def build: ReadMultiColumnBlockRow = ReadMultiColumnBlockRow(
      fixed_builder.build,
      _n_max,
      variable_builder.build
    )
  }

  def builder(schema: sdt.Schema): Builder  = new Builder(schema)

  def multi_column_block_builder(schema: sdt.Schema) = new MultiColumnBlockBuilder(schema)
}

case class ReadBlock(schema: sdt.Schema, worksheet: String,
                     start: PositionAt, read_row: ReadRow,
                     drop_empty_rows: Boolean = true)

case class TableCollectionRead(prog: Map[String, ReadBlock])


class WorkbookReader(file_path: String, table_manager: sdt.TableManager) extends Logging {
  private val _current_sheet = new scala.util.DynamicVariable[Option[XSSFSheet]](None)
  def current_sheet: XSSFSheet = _current_sheet.value match {
    case Some(sheet) => sheet
    case None => throw new IllegalStateException("No current sheet!")
  }

  def parse_value(cell: XSSFCell, cell_type: CellType, field_type: sdt.FieldType): sdt.Value[_] = {
    val tpe = field_type.tpe
    cell_type match {
      case CellType.STRING => {
        val content = cell.getStringCellValue
         tpe match {
          case sdt.VectorType.Char => sdt.Value.char(Some(content))
          case sdt.VectorType.Bool => sdt.Value.empty(field_type)
          case sdt.VectorType.Int => sdt.Value.int(content.toIntOption)
          case sdt.VectorType.Real => sdt.Value.real(content.toDoubleOption)
        }
      }
      case CellType.NUMERIC => {
        val content = cell.getNumericCellValue
        tpe match {
          case sdt.VectorType.Char => sdt.Value.char(Some(content.toString))
          case sdt.VectorType.Bool => sdt.Value.empty(field_type)
          case sdt.VectorType.Int => sdt.Value.int(Some(content.round.toInt))
          case sdt.VectorType.Real => {
            field_type.ext_class match {
              case sdt.Same => sdt.Value.real(Some(content))
              case sdt.DateTime | sdt.Date => {
                val date = DateUtil.getJavaDate(content)
                sdt.Value.date(date)
              }
            }
          }
        }
      }
//      case CellType.BLANK => {
//        sdt.Value(None, field_type)
//      }
      case x => {
        logger.warn(f"Other field type found $x")
        sdt.Value.empty
      }
    }
  }


  def read_value(row: Int, col: Int, field_def: sdt.Field): sdt.Value[_] = {
    val cell = current_sheet.getRow(row).getCell(col)
    val field_type = field_def.field_type
    if (cell == null)
      return sdt.Value.empty(field_type)
    val cell_type = cell.getCellType
    cell_type match {
      case CellType.FORMULA => parse_value(cell, cell.getCachedFormulaResultType, field_type)
      case CellType.BLANK => sdt.Value.empty(field_type)
      case _ => parse_value(cell, cell_type, field_type)
    }
  }

  type ValueMap = Map[String, sdt.Value[_]]

  def read_row(prog: ReadSimpleRow, schema: sdt.Schema, start: PositionAt): (ValueMap, PositionAt) = {
    var row = start.row
    var col = start.col
    val row_values = prog.actions.foldLeft(Map[String, sdt.Value[_]]()) {(acc, action) => {
      action match {
        case Left(n) => {col -= n; acc}
        case Right(n) => {col += n; acc}
        case Read(field_id) => {
          schema.field(field_id) match {
            case Some(f) => acc + (field_id -> read_value(row, col, f))
            case None => throw new IllegalArgumentException(f"No field $field_id present in the schema")
          }
        }
        case Down(n) => {row += n; acc}
        case Up(n) => {row -= n; acc}
        case Column(index) => {col = index; acc}
        case PositionAt(r, c) => {row = r; col = c; acc}
        case AssertValue(value, field_type) => {
          val cell_value = read_value(row, col, sdt.Field("", field_type))
          if (!value.equals(cell_value)) {
            throw new IllegalStateException(s"Value in cell ${(row, col)} is not equal to ${value.v.get}!")
          }
          acc
        }
        case CheckValue(check, field_type) => {
          val cell_value = read_value(row, col, sdt.Field("", field_type))
          if (!check(cell_value)) {
            throw new IllegalStateException(s"Value in cell ${(row, col)} fails check!")
          }
          acc
        }
      }
    }}
    val end_position = PositionAt(row, col)
    (row_values, end_position)
  }

  def read_multi_column_block(prog: ReadMultiColumnBlockRow, schema: sdt.Schema, start: PositionAt): (Seq[ValueMap], PositionAt) = {
    var (fixed_data, end_position) = read_row(prog.fixed, schema, start)
    val result_accumulator = mutable.ArrayBuffer[ValueMap]()
    var i = 0
    while (i < prog.n_max) {
      val row_variable = read_row(prog.variable, schema, end_position)
      val variable_data = row_variable._1
      end_position = row_variable._2
      if (!(variable_data.forall { case (k, v) => v.is_na} && prog.drop_empty_blocks)) {
        result_accumulator += (fixed_data ++ variable_data)
      }
      i += 1
    }
    (result_accumulator.toSeq, end_position)

  }

  def read_block(prog: ReadBlock, workbook: XSSFWorkbook): sdt.Table = {
    val sheet = workbook.getSheet(prog.worksheet)
    _current_sheet.withValue(Some(sheet)) {
      val corner = prog.start
      val builder = table_manager.incremental_table_builder(prog.schema)
      var row_index = prog.start.row
      for (row <- sheet.rowIterator().asScala) {
        if (row.getRowNum >= row_index) {
          prog.read_row match {
            case rr: ReadSimpleRow => {
              val (values, _) = read_row(rr, prog.schema, prog.start.copy(row = row_index))
              if (prog.drop_empty_rows && values.forall(x => x._2.is_na)) {
              } else {
                builder.append_value_map(values)
              }
            }
            case rr: ReadMultiColumnBlockRow => {
              val (values, _) = read_multi_column_block(rr, prog.schema, prog.start.copy(row = row_index))
              values.foreach(v => builder.append_value_map(v))
            }
          }
          row_index += 1
        }
      }
      builder.toTable
    }
  }

  def read_table_collection(tc: TableCollectionRead): Map[String, sdt.Table] = {
    Using(new FileInputStream(file_path)) (fs => {
      Using(new XSSFWorkbook(fs)) (workbook => {
        tc.prog.map {case (table_name: String, block_prog : ReadBlock) => {
          logger.info(f"Reading table '$table_name' from sheet '${block_prog.worksheet}'")
          (table_name, read_block(block_prog, workbook))
        }}.toMap

      }).get
    }).get

  }

}


