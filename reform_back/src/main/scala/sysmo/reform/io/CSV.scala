package sysmo.reform.io



import java.io.File

import kantan.csv.{CsvConfiguration, HeaderDecoder}
import monix.eval.Task
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, OverflowStrategy}

import scala.util.{Failure, Success, Try, Using}


object CSVReader {
  import kantan.csv._ // All kantan.csv types.
  import kantan.csv.ops._ // Enriches types with useful methods.
  import kantan.csv.generic._ // Automatic derivation of codecs.

  def read_seq[T](path: String, conf: CsvConfiguration)(implicit header_decoder: HeaderDecoder[T]): Seq[T] = {
    Using(new File(path).asCsvReader[T](conf)) {
      reader => reader.map{
        case Left(e) => throw new RuntimeException("Failed reading a record")
        case Right (x) => x
      }.toSeq
    } match {
      case Success(x) => x
      case Failure(e) => throw e
    }
  }


  def read[T](path: String, conf: CsvConfiguration)(implicit header_decoder: HeaderDecoder[T]): Observable[T] = {
    val reader = new File(path).asCsvReader[T](conf)
    var it = reader.iterator
    Observable.create[T](OverflowStrategy.Unbounded) {
      sub => {
        var ack : Ack  = Ack.Continue
        for (elem <-it) {
          elem match {
            case Left(e) => sub.onError(e)
            case Right(x) => sub.onNext(x)
          }

        }
        sub.onComplete()
        Cancelable.empty
      }
    }.guarantee(Task {
      reader.close()
      println("Closed csv file")
    })
  }
//    val on_error : Throwable => T = ex => {
//      println("Couldn't read");
//      PatientRecord("a", "b", "c", 6, "e", "f")
//    }
//    val stream = Observable.fromIterable(reader.toIterable)
//    .map {
//      case x@Left(e) => {println("Error", e); x}
//      case _ => _
//    }
//    val stream = Observable.
//    .collect {
//      case Right(x) => x
//    }
//    stream
////    reader.close()
//  }
}

object CSVWriter{
  import kantan.csv._ // All kantan.csv types.
  import kantan.csv.ops._ // Enriches types with useful methods.

  def write[T](values : Seq[T], path : String, conf : CsvConfiguration)(implicit header_encoder : HeaderEncoder[T]) : Unit = {
    Using(new File(path).asCsvWriter[T](conf)) {
      writer => {
        for (v <- values) {
          writer.write(v)
        }
      }
    }
  }
}

