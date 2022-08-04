package sysmo.reform.shared.table2

import sysmo.reform.shared.{field => F}

trait Table {
  def schema: Table.Schema
  def nrow: Int
//  def ncol: Int
  def get(row_id: Int, col_id: Int): F.Value[_]
//  def column(index: Int): Series
//  def column(name: String): Series
  def row(row_id: Int): Table.Row
  def row_iter: Iterator[Table.Row]
}

object Table {
  type Row = F.Record
  type Schema = F.Schema
}
