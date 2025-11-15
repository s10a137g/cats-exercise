package day4

import java.util.concurrent.Executors
import scala.collection.BuildFrom
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

/**
 * Day4 exercise skeleton for learning about threads and thread pools in Scala.
 *
 * In this exercise you will explore how to create and use custom thread pools via
 * `ExecutionContext.fromExecutorService`, simulate blocking I/O work, and compare
 * the performance of the global `ExecutionContext` versus a dedicated fixed thread pool.
 *
 * Follow the TODO markers below to implement the required functions.
 */
object ThreadPoolExercise extends App {

  /**
   * Create a custom fixed thread pool ExecutionContext.
   *
   * Use `Executors.newFixedThreadPool(size)` to create a Java ExecutorService with
   * a fixed number of threads, then wrap it with `ExecutionContext.fromExecutorService`.
   *
   * @param size the number of threads to allocate in the pool
   * @return a new ExecutionContext backed by a fixed thread pool
   */
  def fixedThreadPool(size: Int): ExecutionContext = {
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(size))
  }

  /**
   * Simulate an I/O-bound task asynchronously.
   *
   * This function should return a Future that sleeps for `millis` milliseconds
   * (to simulate blocking work) and then returns the provided `id` so that you can
   * identify which task completed. Use the supplied ExecutionContext to run the Future.
   *
   * Hint: you can call `Thread.sleep(millis)` inside the Future body.
   *
   * @param id an identifier for the task (will be returned after the delay)
   * @param millis how long the simulated I/O should block (in milliseconds)
   * @param ec the ExecutionContext on which to run the Future
   * @return a Future containing the id once the sleep completes
   */
  def simulateIO(id: Int, millis: Long)(implicit ec: ExecutionContext): Future[Int] = {
    Future{
      Thread.sleep(millis)
      id
    }(ec)
  }

  /**
   * Run a sequence of simulated I/O tasks concurrently using the provided ExecutionContext.
   *
   * Given a list of durations, start one Future per element by calling `simulateIO`.
   * Use `Future.traverse` to turn the list of Futures into a single Future of a list.
   *
   * @param durations a list of (taskId, durationInMillis) pairs
   * @param ec the ExecutionContext to run the Futures on
   * @return a Future with the list of completed taskIds in the order they finish
   */
  def runTasks(durations: List[(Int, Long)])(implicit ec: ExecutionContext): Future[List[Int]] = {
    val bf = implicitly[BuildFrom[List[(Int, Long)], Int, List[Int]]]

    Future.traverse(durations) { case (id, millis) =>
      simulateIO(id, millis)(ec)
    }(bf, ec)
  }

  /**
   * Measure the time it takes to run the tasks with the given ExecutionContext.
   *
   * Use `System.nanoTime()` to capture the start and end times, then convert the
   * difference to milliseconds. Return both the duration and the list of completed taskIds.
   *
   * @param name a label for the run (e.g. "global", "fixed-4")
   * @param durations the tasks to run
   * @param ec the ExecutionContext to run on
   * @return a Future containing a tuple of (elapsedMillis, results)
   */
  def timedRun(name: String, durations: List[(Int, Long)])(implicit ec: ExecutionContext): Future[(Long, List[Int])] = {
    // TODO: implement this method. Capture start time, run the tasks, await the result,
    // then capture end time and compute the elapsed duration.
    println(s"$name run took … ms")
    val start = System.nanoTime()
    runTasks(durations)(ec).map {result =>
      val elapsedMillis = (System.nanoTime() - start) / 1000000
      (elapsedMillis, result)
    }(ec)
  }

  // Main test harness.
  // You can run this program (e.g. via `sbt run` or IntelliJ) once you've filled in the TODOs.
  // It will run the same set of tasks on the global ExecutionContext and on a custom fixed thread pool.
  // Observe the printed durations to see how using a fixed thread pool affects concurrency.
  val tasks = List(
    (1, 1000L),
    (2, 1000L),
    (3, 1000L),
    (4, 1000L),
    (5, 1000L)
  )

  // Run with the global ExecutionContext
  implicit val globalEc: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val globalRun = timedRun("global", tasks)(globalEc)
  val globalResult = scala.concurrent.Await.result(globalRun, 5.seconds)
  println(s"Global run took ${globalResult._1} ms and returned ${globalResult._2}")

  // Run with a fixed thread pool of size 2
  val poolEc = fixedThreadPool(2)
  val poolRun = timedRun("fixed-2", tasks)(poolEc)
  val poolResult = scala.concurrent.Await.result(poolRun, 5.seconds)
  println(s"Fixed thread pool run took ${poolResult._1} ms and returned ${poolResult._2}")

  // Don't forget to shut down the ExecutorService backing your custom ExecutionContext
  poolEc match {
    case fromService: ExecutionContextExecutorService =>
      fromService.shutdown()
    case _ =>
      // nothing to shut down
  }
}