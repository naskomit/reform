package sysmo.reform.db

import cats.effect.Resource
import monix.eval.Task
import monix.reactive.Observable

import scala.concurrent
import slick.jdbc.{JdbcActionComponent, JdbcProfile}
import slick.jdbc.meta.MTable

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Using}

import sysmo.reform.util.Logging


trait DBConfiguration extends Logging {
  val jdbc_profile: JdbcProfile
  def connection : jdbc_profile.api.Database
  def with_connection[U](f: jdbc_profile.api.Database => U): U = {
    Using(connection) {
      db => f(db)
    }} match {
    case Success(x) => x
    case Failure(e) => {
      logger.error("Failed with connection")
      throw new RuntimeException("Failed", e)
    }
  }
}


trait AppDatabaseStorage extends Logging {

  val db_config: DBConfiguration
  import db_config.jdbc_profile.api._

  def all_tables: Seq[TableQuery[_ <: Table[_]]]
  def all_schemas: db_config.jdbc_profile.SchemaDescription = {
    val (first, rest) = all_tables.splitAt(1)
    rest.foldLeft(first.head.schema)((acc, table) =>  acc ++ table.schema)
  }

  def schema_methods: JdbcActionComponent#SchemaActionExtensionMethods = all_schemas
  implicit val ec: ExecutionContext

  def conn : Resource[Task, Database] = Resource.make {
    Task(
      db_config.connection
    )
  } {
    c => Task.deferFuture(
      c.shutdown
    )
  }

  private def run[R](action : DBIOAction[R, NoStream, Nothing], log : Boolean = true): Task[R] =
    conn.use(x => Task.deferFuture {
      x.run(action)
    })

  def list_tables: Task[Vector[MTable]] = run(MTable.getTables)
  def empty_table[U](table : TableQuery[_ <: Table[_]]): Task[Unit] = run(DBIO.seq(table.delete))

  def initialize_schema: Task[Unit] = {
    list_tables.flatMap(tables => {
      if (tables.isEmpty) {
        val create_sql = schema_methods.create
        Task {
          logger.info("Creating schema")
          logger.info(create_sql.statements.mkString("\n"))
        } >>
        run(schema_methods.create) >>
        Task {
          logger.info("Schema created")
        }

      }
      else {
        Task {
          logger.info("Schema exists")
        }
      }
    })
  }

  def empty_tables: Task[Unit] = Task {
    logger.info("Emptying DB tables")
  } >> run(DBIO.seq(all_tables.reverse.map(x => x.delete): _*))

  def insert_batch[U](table: TableQuery[_ <: Table[U]],
                      data: Seq[U]): Future[Unit] = {
    db_config.with_connection(db => {
      val sql = DBIO.seq(table ++= data)
      db.run(sql)
        .map(x => {
          logger.info(f"Inserted batch of ${data.length} records"); x
        })
    })
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
//      logger.info("Inserting batch")
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



