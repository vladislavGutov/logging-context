package com.github

import cats.effect.Sync
import cats.syntax.functor._
import io.chrisdavenport.log4cats.{Logger, StructuredLogger}

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

}
