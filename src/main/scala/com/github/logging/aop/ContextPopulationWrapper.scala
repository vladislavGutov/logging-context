package com.github.logging.aop

import java.util.Collection
import java.util.concurrent.Callable

import com.github.logging.LoggingContext
import org.aspectj.lang.ProceedingJoinPoint

import scala.jdk.CollectionConverters._

trait ContextPopulationWrapper {
  def handleRunnableArgument(jp: ProceedingJoinPoint): AnyRef = {
    handleArgumentAt[Runnable](jp, 0)(wrapRunnable)
  }

  def handleCallableArgument(jp: ProceedingJoinPoint): AnyRef = {
    handleArgumentAt[Callable[_]](jp, 0)(wrapCallable)
  }

  def handleCallableCollectionArgument(jp: ProceedingJoinPoint): AnyRef = {
    handleArgumentAt[Collection[Callable[_]]](jp, 0)(wrapCallableCollection)
  }

  private def wrapCallableCollection(tasks: Collection[Callable[_]]): Collection[Callable[_]] = {
    tasks.asScala.map(wrapCallable).toSeq.asJava
  }

  private def handleArgumentAt[T](jp: ProceedingJoinPoint, position: Int)(f: T => AnyRef): AnyRef = {
    val arguments = jp.getArgs
    arguments(position) = f(arguments(position).asInstanceOf[T])
    jp.proceed(arguments)
  }

  private def wrapRunnable(underlying: Runnable): Runnable = {
    val currentContext = LoggingContext.localContext.value;
    () =>
      {
        LoggingContext.localContext.value = currentContext;
        underlying.run();
      }
  }

  private def wrapCallable(underlying: Callable[_]): Callable[_] = {
    val currentContext = LoggingContext.localContext.value;
    () =>
      {
        LoggingContext.localContext.value = currentContext;
        underlying.call();
      }
  }
}
