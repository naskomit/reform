package sysmo.typelevel

object ScalaWithCats {
  import cats._
  import cats.implicits._

  /** 2.5.3 Monoid Syntax */
  def monoid_syntax() = {
    val stringResult = "Hi " |+| "there" |+| Monoid[String].empty
    println(stringResult)
  }

  /** 4. Monads */
  /** 4.2.2 Default Instances */
  def monad_default_instances() = {
    val x = Monad[Option].flatMap(Option(5))(x => Some(x * 2))
    println(x)
  }

  /**  */

  /**  */

  /**  */

  /**  */

  def run() = {
    monoid_syntax()
    monad_default_instances()
  }

}
