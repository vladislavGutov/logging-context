package com.github

import cats.effect.{ExitCode, IO, IOApp}
import com.github.logging._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.instances.list._
import cats.syntax.parallel._
import cats.syntax.functor._
import cats.syntax.apply._

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

    (0 to 10).toList.parTraverse(run)
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
      _ <- IO.shift
      result <- opInner(id, tpe).withLogContext(_.tpe(tpe))
      _ <- IO.shift
      _      <- log.info(s"op end [static id = $id]")
    } yield result
  }

  private def opInner(id: String, tpe: String): IO[String] =
    for {
      _      <- log.info(s"op_inner start [static id = $id, static type = $tpe]")
      _ <- IO.shift
      result <- IO("user")
      _ <- IO.shift
      _      <- log.info(s"op_inner end [static id = $id, static type = $tpe]")
    } yield result



  private val log: Logger[IO] = Slf4jLogger.getLogger[IO].mdc
}