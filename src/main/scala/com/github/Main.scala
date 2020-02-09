package com.github

import java.util.concurrent.{Executor, Executors}

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.instances.list._
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.parallel._
import com.github.logging._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object Sequential extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _    <- log.info("Start")
      user <- op.withLogContext(_.id("1"))
      _    <- log.info(s"result: $user")
      _    <- log.info("End")
    } yield ExitCode.Success
  }

  private def op: IO[String] = {
    for {
      _      <- log.info("op start")
      result <- opInner.withLogContext(_.tpe("inner"))
      _      <- log.info("op end")
    } yield result
  }

  private def opInner: IO[String] =
    for {
      _      <- log.info("op_inner start")
      result <- IO("user")
      _      <- log.info("op_inner end")
    } yield result

  private val log: Logger[IO] = Slf4jLogger.getLogger[IO].mdc

}

object Concurrent extends IOApp {

  private val types = Vector("type1", "type2", "type3")

  override def run(args: List[String]): IO[ExitCode] = {

    (0 to 10).toList
      .parTraverse(run)
      .as(ExitCode.Success)

  }

  private def run(id: Int): IO[String] = {
    val idx = id % types.length
    val tpe = types(idx)
    IO.shift *>
      op(id.toString, tpe).withLogContext(_.id(id.toString)) <* IO.shift
  }

  private def op(id: String, tpe: String): IO[String] = {
    for {
      _      <- log.info(s"op start [static id = $id]")
      _      <- IO.shift
      result <- opInner(id, tpe).withLogContext(_.tpe(tpe))
      _      <- IO.shift
      _      <- log.info(s"op end [static id = $id]")
    } yield result
  }

  private def opInner(id: String, tpe: String): IO[String] =
    for {
      _      <- log.info(s"op_inner start [static id = $id, static type = $tpe]")
      _      <- IO.shift
      result <- IO("user")
      _      <- IO.shift
      _      <- log.info(s"op_inner end [static id = $id, static type = $tpe]")
    } yield result

  override protected implicit def contextShift: ContextShift[IO] = IO.contextShift(
    ExecutionContext.global.wrapLogging
  )

  override protected implicit def timer: Timer[IO] = IO.timer(
    ExecutionContext.global.wrapLogging,
    Executors.newScheduledThreadPool(2).wrapLogging
  )

  private val log: Logger[IO] = Slf4jLogger.getLogger[IO].mdc
}

object ConcurrentNoAccessToExecutor extends IOApp {

  def part1(text: String, id: String): IO[String] = {
    for {
      _      <- log.info(s"start part1 [static id = $id]")
      result <- IO(text)
      _      <- log.info(s"end part1 [static id = $id]")
    } yield result
  }

  def part2(text: String, id: String): IO[String] = {
    for {
      _      <- log.info(s"start part2 [static id = $id]")
      result <- IO(text)
      _      <- log.info(s"end part2 [static id = $id]")
    } yield result

  }

  def combine(id: String, tp: ThirdParty): IO[Unit] = {
    for {
      _       <- log.info(s"start combine [static id = $id]")
      r1      <- part1("hi", id)
      callRes <- accessTP(tp, id)
      r2      <- part2(r1 + callRes, id)
    } yield r2
  }

  private def accessTP(tp: ThirdParty, id: String): IO[String] =
    IO.async[String](cb => tp.access(id, cb)) <* IO.shift

  override def run(args: List[String]): IO[ExitCode] = {

    ThirdParty.apply.use { tp =>
      (0 to 10).toList
        .map(_.toString)
        .parTraverse(id => combine(id, tp).withLogContext(_.id(id)))
        .as(ExitCode.Success)
    }

  }

  override protected implicit def contextShift: ContextShift[IO] = IO.contextShift(
    ExecutionContext.global.wrapLogging
  )

  override protected implicit def timer: Timer[IO] = IO.timer(
    ExecutionContext.global.wrapLogging,
    Executors.newScheduledThreadPool(2).wrapLogging
  )

  private val log: Logger[IO] = Slf4jLogger.getLogger[IO].mdc
}

class ThirdParty(executor: Executor) {

  def access(id: String, cb: Either[Throwable, String] => Unit): Unit = {
    executor.execute { () =>
      executor.execute { () =>
        cb(Right(id))
      }
    }
  }
}

object ThirdParty {

  case class Request(id: String, cb: Either[Throwable, String] => Unit)

  def apply: Resource[IO, ThirdParty] = {
    Resource
      .make(IO(Executors.newFixedThreadPool(10)))(tp => IO(tp.shutdown()))
      .map(executor => new ThirdParty(executor))
  }
}
