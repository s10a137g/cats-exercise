package day3

import cats.data.EitherT
import cats.syntax.all._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.Try

/**
 * Day3 exercise skeleton for learning EitherT.
 *
 * The goal of this exercise is to practise using the Cats `EitherT` monad
 * transformer to compose error‑handling logic (using `Either`) with asynchronous
 * computations (using `Future`).  Fill in the TODOs to make the program
 * compile and run.  You will implement two helper functions that return
 * `Future[Either[String, Double]]` and then compose them using `EitherT`.
 */
object EitherTExercise extends App {

  /**
   * Parse a String into a Double asynchronously.
   *
   * If the string is a valid floating‑point number, return a `Right` of the
   * parsed double.  Otherwise, return a `Left` containing an error message.
   *
   * Hint: you can use `scala.util.Try` to attempt the parse, then
   * convert the result to an `Either` with `toEither`, and finally wrap
   * it in a `Future.successful`.
   */
  def parseDoubleAsync(s: String)(implicit ec: ExecutionContext): Future[Either[String, Double]] = {
    Future.successful(
      s.toDoubleOption.toRight(s"Don't parse double $s")
    )
  }

  /**
   * Perform a division asynchronously, returning an error if the divisor is zero.
   *
   * If `b` is zero, return a `Left` with a message like "Cannot divide by zero".
   * Otherwise, return a `Right` of the quotient.
   */
  def divideAsync(a: Double, b: Double)(implicit ec: ExecutionContext): Future[Either[String, Double]] = {
    // TODO: implement this method
      Future.successful(Either.cond(b != 0,  a /b, "Cannot divide by zero"))
  }

  /**
   * Compose the parsing and division operations using EitherT.
   *
   * Given two strings, attempt to parse them to doubles asynchronously and
   * then divide them.  Use a for‑comprehension over `EitherT` to chain
   * operations and short‑circuit on the first error.  The return type
   * `EitherT[Future, String, Double]` makes it easy to combine multiple
   * asynchronous steps with error handling.
   */
  def divisionProgramAsync(inputA: String, inputB: String)(implicit ec: ExecutionContext): EitherT[Future, String, Double] = {
    // TODO: complete the for‑comprehension below
    for {
      // convert parseDoubleAsync results into EitherT
      a <- EitherT(parseDoubleAsync(inputA))
      b <- EitherT(parseDoubleAsync(inputB))
      // lift the result of divideAsync into EitherT as well
      result <- EitherT(divideAsync(a, b))
    } yield result
  }

  // Provide an implicit ExecutionContext.  We'll use the global execution
  // context for simplicity, but in real applications you might use a
  // dedicated thread pool.  See Day2 materials for details.
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  // Use this main block to test your implementation.
  // Uncomment and fill in the TODOs to run the program.
  val success = divisionProgramAsync("4", "2").value
  println(scala.concurrent.Await.result(success, 1.second)) // Should print Right(2.0)
  val failure = divisionProgramAsync("a", "b").value
  println(scala.concurrent.Await.result(failure, 1.second)) // Should print Left("...error message...")
}