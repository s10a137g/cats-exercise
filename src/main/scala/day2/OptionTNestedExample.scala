package day2

import cats.data.OptionT
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object OptionTNestedExample extends App {

  val foo = OptionT[Future, Int](Future.successful(Some(10)))
  val bar = OptionT[Future, Int](Future.successful(Some(5)))
  val baz = OptionT[Future, Int](Future.successful(Some(2)))

  val combined: OptionT[Future, Int] = for {
    a <- foo
    b <- bar
    c <- baz
  } yield a + b + c

  val value = Await.result(combined.value, 1.second)
  println(value)
}
