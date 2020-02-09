package com.github.logging

import cats.effect.Sync

import scala.util.DynamicVariable

case class LoggingContext(map: Map[String, String]) {
  def id(id: String): LoggingContext = LoggingContext(map + ("id" -> id))

  def tpe(tpe: String): LoggingContext = LoggingContext(map + ("type" -> tpe))
}

object LoggingContext {

  def apply(): LoggingContext = LoggingContext(Map())

  private[logging] val localContext: DynamicVariable[LoggingContext] =
    new DynamicVariable[LoggingContext](LoggingContext())

  def get[F[_]](implicit F: Sync[F]): F[LoggingContext] = F.delay {
    localContext.value
  }

  def update[F[_]](f: LoggingContext => LoggingContext)(implicit F: Sync[F]): F[LoggingContext] =
    F.delay {
      val old = localContext.value
      localContext.value = f(old)
      old
    }

  def set[F[_]](value: LoggingContext)(implicit F: Sync[F]): F[LoggingContext] = update(_ => value)

}
