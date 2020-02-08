package com.github

import java.util.concurrent.ScheduledExecutorService

import cats.effect.Sync
import cats.syntax.functor._
import io.chrisdavenport.log4cats.{Logger, StructuredLogger}

import scala.concurrent.ExecutionContext

package object logging {

  implicit class MdcExtension[F[_] : Sync](logger: StructuredLogger[F]) {
    def mdc: Logger[F] = new MdcLogger[F](logger)
  }

  implicit class ContextAwareSync[F[_], A](fa: F[A])(implicit F: Sync[F]) {

    def withLogContext(f: LoggingContext => LoggingContext): F[A] =
      F.bracket {
        LoggingContext.update(f)
      } { _ =>
        fa
      } { old =>
        LoggingContext.set(old).void
      }
  }

  implicit class LoggingEC(ec: ExecutionContext) {
    def wrapLogging: ExecutionContext = new LoggingExecutionContext(ec)
  }

  implicit class LoggingSES(service: ScheduledExecutorService) {
    def wrapLogging: ScheduledExecutorService = new LoggingScheduledExecutorService(service)
  }

}
