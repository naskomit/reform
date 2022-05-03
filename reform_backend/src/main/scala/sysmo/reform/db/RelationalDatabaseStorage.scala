package sysmo.reform.db

import cats.effect.Resource
import monix.eval.Task
import slick.jdbc.{JdbcActionComponent, JdbcProfile}
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}
import sysmo.reform.util.{AsyncLogging}
import sysmo.reform.shared.{query => Q}


trait DBConfiguration extends AsyncLogging {
  val jdbc_profile: JdbcProfile
  def connection : jdbc_profile.api.Database
//  def with_connection[U](f: jdbc_profile.api.Database => U): U = {
//    Using(connection) {
//      db => f(db)
//    }} match {
//    case Success(x) => x
//    case Failure(e) => {
//      logger.error("Failed with connection")
//      throw new RuntimeException("Failed", e)
//    }
//  }
}


trait RelationalDatabaseStorage extends AsyncLogging {

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

  /** Task to run any DB actions */
  protected def run[R](action : DBIOAction[R, NoStream, Nothing], log : Boolean = true): Task[R] =
    conn.use(x => Task.deferFuture {
      x.run(action)
    })

  /** Base effects */

  def query[Tbl <: Table[Rec], Rec](q: Query[Tbl, Rec, Seq]): Task[Seq[Rec]] = {
    val q1: JdbcActionComponent#StreamingQueryActionExtensionMethods[Seq[Rec], Rec] = q
    logger.info(
      q1.result.statements.mkString("\n")
    ) >>
    run(
      q1.result
    )
    .flatMap(x => {
      logger.info(f"Fetched ${x.length} records") >>
      Task(x)
     })
  }

  /** */
  def list_tables: Task[Vector[MTable]] = run(MTable.getTables)

  /** Create all the tables defined in the schema */
  def create_schema(): Task[Unit] = {
    val sql = schema_methods.create
    logger.info(sql.statements.mkString("\n")) >>
    run(sql) >>
    logger.info("Schema created")
  }

  def drop_schema(): Task[Unit] = {
    val sql = schema_methods.drop
    logger.info(sql.statements.mkString("\n")) >>
    run(sql) >>
    logger.info("Schema dropped")
  }


  /** Remove content from all the tables */
  def empty_tables: Task[Unit] = {
    logger.warn("Emptying DB tables") >>
    run(DBIO.seq(all_tables.reverse.map(x => x.delete): _*))
  }

  /** Higher order functionality*/
  def initialize_schema: Task[Unit] = {
    list_tables.flatMap(tables => {
      if (tables.isEmpty)
        Task {}
      else {
        drop_schema()
      }
    }) >> create_schema()
  }

  def insert_batch[U](table: TableQuery[_ <: Table[U]],
                      data: Seq[U]): Task[Unit] = {
    val sql = DBIO.seq(table ++= data)
    logger.info(f"Inserting ${data.length} records into ${table.schema}") >>
    run(sql)
  }

  def use_query_filter[Rec](table: Table[Rec], q : Q.QueryFilter) = {
    table
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



