package day1

import cats.data.OptionT
import cats.implicits._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object OptionTExample extends App {

  // OptionTを使わない場合
  private val notOptionTResult1: Future[Option[Int]] = Future.successful(Some(20))
  private val notOptionTResult2: Future[Option[Int]] = Future.successful(Some(30))

  private val combinedNotOptionT: Future[Option[Int]] = for {
    aOpt <- notOptionTResult1
    bOpt <- notOptionTResult2
  } yield for {
    a <- aOpt
    b <- bOpt
  } yield a + b

  private val notOptionTValue = Await.result(combinedNotOptionT, 1.second)

  println(s"Not OptionT Result: $notOptionTValue") // Should print: Not OptionT Result: Some(50)

  // OptionTを使う場合
  private val result1 = OptionT[Future, Int](Future(Some(10)))
  private val result2 = OptionT[Future, Int](Future(Some(5)))

  private val combined: OptionT[Future, Int] = for {
    a <- result1
    b <- result2
  } yield a + b

  val value = Await.result(combined.value, 1.second)
  println(s"Result: $value") // Should print: Result: Some(15)
}
