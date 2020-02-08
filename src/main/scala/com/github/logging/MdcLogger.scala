package com.github.logging

import cats.effect.Sync
import cats.syntax.flatMap._
import io.chrisdavenport.log4cats.{Logger, StructuredLogger}

class MdcLogger[F[_] : Sync](logger: StructuredLogger[F]) extends Logger[F] {

  @inline
  private def withCtx(f: LoggingContext => F[Unit]): F[Unit] =
    LoggingContext.get[F].flatMap(f)

  override def error(t: Throwable)(message: => String): F[Unit] =
    withCtx(c => logger.error(c.map, t)(message))

  override def warn(t: Throwable)(message: => String): F[Unit] =
    withCtx(c => logger.warn(c.map, t)(message))

  override def info(t: Throwable)(message: => String): F[Unit] =
    withCtx(c => logger.info(c.map, t)(message))

  override def debug(t: Throwable)(message: => String): F[Unit] =
    withCtx(c => logger.debug(c.map, t)(message))

  override def trace(t: Throwable)(message: => String): F[Unit] =
    withCtx(c => logger.trace(c.map, t)(message))

  override def error(message: => String): F[Unit] =
    withCtx(c => logger.error(c.map)(message))

  override def warn(message: => String): F[Unit] =
    withCtx(c => logger.warn(c.map)(message))

  override def info(message: => String): F[Unit] =
    withCtx(c => logger.info(c.map)(message))

  override def debug(message: => String): F[Unit] =
    withCtx(c => logger.debug(c.map)(message))

  override def trace(message: => String): F[Unit] =
    withCtx(c => logger.trace(c.map)(message))

}
