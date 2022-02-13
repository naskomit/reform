package sysmo.reform.db

import monix.eval.Task
import monix.reactive.{Consumer, Observable}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.{JdbcActionComponent, JdbcProfile, SQLiteProfile}
import slick.jdbc.meta.MTable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Using}
import monix.execution.Scheduler.Implicits.global

trait AppDatabaseStorage {
  val jdbc_profile: JdbcProfile
  import jdbc_profile.api._

  val all_tables: Seq[TableQuery[_ <: Table[_]]]
  val all_schemas: jdbc_profile.SchemaDescription
  val schema_methods: JdbcActionComponent#SchemaActionExtensionMethods
  implicit val ec: ExecutionContext

  def initialize_schema(db: Database): Future[Unit] = {
    db.run(MTable.getTables).flatMap(tables =>
      if (tables.isEmpty) {
        db.run(schema_methods.create).andThen {
          case Success(_) => println("Schema created")
        }
      } else {
        println("Schema exists")
        Future.successful()
      }
    )
  }

  def empty_tables(db: Database): Future[Unit] = {
    val queries = DBIO.seq(
      all_tables.map(x => x.delete): _*
    )

    db.run(queries)
    Future.successful()
  }

  def insert_batch[U](db: Database, table: TableQuery[_ <: Table[U]],
                      data: Seq[U]): Future[Unit] = {
      val sql = DBIO.seq(table ++= data)
      db.run(sql)
        .map(x => {println(f"Inserted batch of ${data.length} records"); x})

  }

  def read_table[U](db: Database, table: TableQuery[_ <: Table[U]]): Future[Seq[U]] = {
    val sql = table
    db.run(sql.result)
  }

//  def insert_data[U](db: Database, table: TableQuery[_ <: Table[U]],
//                     data: Observable[U]): Future[Unit] = {
//    val batch_size = 1
//    def insert() = {
//      data.take(batch_size).toListL.runToFuture.map(
//        insert_batch[U](db, table, _)
//      )
//    }
//
//    val consumer = Consumer.foldLeftTask[Future[Unit], U](Future.successful())((acc, elem) => {
//      println("Inserting batch")
//      Thread.sleep(1000)
//      acc.map(insert_batch[U](db, table, _))
//    })
//
//    data.consumeWith(consumer).runToFuture
//
////    data.bufferTumbling(batch_size).map { batch =>
////        insert_batch[U](db, table, batch)
////    }.toListL {}
//  }
}



