package sysmo.reform.data

import io.circe.Json
import io.circe.syntax._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import sysmo.reform.shared.data.{UUObjectId, Value}
import sysmo.reform.shared.data.Transport._
import sysmo.reform.shared.table.Transport._
import sysmo.reform.shared.types.Transport._
import sysmo.reform.shared.table.Table.Row
import sysmo.reform.shared.table.{LocalRowBasedTable, LocalTable, Table}
import sysmo.reform.shared.types.{RecordFieldType, RecordType}

import java.util.{Date, UUID}


class TestTransport extends AnyFunSpec with Matchers {
  import Value.implicits._

  describe("Serialization/Deserialization") {
    val v_e1 = Value.empty
    val v_e2: Value = Value.RealValue(None)

    val v_real = Value(2.5)
    val v_int = Value(63)
    val v_long = Value(1234232L)
    val v_bool1 = Value(true)
    val v_bool2 = Value(false)
    val v_char = Value("Hello")
    val date_long = 1667378226371L
    val v_date = Value(new Date(date_long))
    val uuid =  "5fd1c681-d6e1-4ede-8b8b-1c1929525869"
    val v_id = Value(UUObjectId(UUID.fromString(uuid)))

    it("Serialize value") {

      assert(v_e1.asJson == Json.Null)
      assert(v_e2.asJson == Json.Null)
      assert(v_real.asJson == Json.fromDouble(2.5).get)
      assert(v_int.asJson == Json.fromInt(63))
      assert(v_long.asJson == Json.fromLong(1234232L))
      assert(v_bool1.asJson == Json.fromBoolean(true))
      assert(v_bool2.asJson == Json.fromBoolean(false))
      assert(v_char.asJson == Json.fromString("Hello"))
      assert(v_date.asJson == Json.fromLong(date_long))
      assert(v_id.asJson == Json.fromString(uuid))
    }

    val fc = RecordFieldType.constr
    val schema = (RecordType("Table 1")
      + fc.f_real("Real") + fc.f_int("Int")
      + fc.f_long("Long") + fc.f_bool("Bool")
      + fc.f_char("Char")
//      + fc.f_date("Date") + fc.f_id("Id")
      ).build

    it("Serialize/Deserialize schema") {
      schema.asJson.as[RecordType] match {
        case Left(err) => assert(false, err)
        case Right(schema1) => assert(schema == schema1)
      }
    }

    val row: Row = Table.Row.SeqRow(schema, Seq(
      v_real, v_int, v_long, v_bool1, v_char
      //, v_date, v_id
    ))
    val table: LocalTable = LocalRowBasedTable(schema, Seq(row))

    it("Serialize row") {
      val row_data = Seq(
        2.5.asJson, 63.asJson,
        1234232L.asJson, true.asJson,
        "Hello".asJson,
//        1667378226371L.asJson, "5fd1c681-d6e1-4ede-8b8b-1c1929525869".asJson
      )

      assert(row.asJson == Json.fromValues(row_data))
    }


    it("Serialize/Deserialize table") {
      val table_json = table.asJson
      table_json.as[LocalTable] match {
        case Left(err) => assert(false, err)
        case Right(table1) => {
          for (i <- 0 until table.ncol) {
            assert(table.get(0, i) == table1.get(0, i))
          }
        }
      }
    }
  }
}
