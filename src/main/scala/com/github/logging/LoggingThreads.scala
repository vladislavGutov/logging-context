package com.github.logging

import java.util
import java.util.concurrent.{Callable, Future, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._


class LoggingExecutionContext(ec: ExecutionContext) extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = {
    val context = LoggingContext.localContext.value
    ec.execute(new WrappedRunnable(runnable, context))
  }

  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
}

class LoggingScheduledExecutorService(service: ScheduledExecutorService) extends ScheduledExecutorService {

  private def unsafeGetContext: LoggingContext = LoggingContext.localContext.value

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] = {
    service.schedule(
      new WrappedRunnable(command, unsafeGetContext), delay, unit)
  }

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] = {
    service.schedule(
      new WrappedCallable[V](callable, unsafeGetContext), delay, unit
    )
  }

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] = {
    service.scheduleAtFixedRate(
      new WrappedRunnable(command, unsafeGetContext), initialDelay, period, unit)
  }

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] = {
    service.scheduleWithFixedDelay(
      new WrappedRunnable(command, unsafeGetContext), initialDelay, delay, unit)
  }

  override def shutdown(): Unit = service.shutdown()

  override def shutdownNow(): util.List[Runnable] = service.shutdownNow()

  override def isShutdown: Boolean = service.isShutdown

  override def isTerminated: Boolean = service.isTerminated

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = service.awaitTermination(timeout, unit)

  override def submit[T](task: Callable[T]): Future[T] = {
    service.submit(
      new WrappedCallable[T](task, unsafeGetContext))
  }

  override def submit[T](task: Runnable, result: T): Future[T] = {
    service.submit(
      new WrappedRunnable(task, unsafeGetContext), result)
  }

  override def submit(task: Runnable): Future[_] = {
    service.submit(new WrappedRunnable(task, unsafeGetContext))
  }

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[Future[T]] = {
    val context = unsafeGetContext
    val mappedTasks = tasks.asScala.map(c => new WrappedCallable[T](c, context)).asJavaCollection
    service.invokeAll(mappedTasks)
  }

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[Future[T]] = {
    val context = unsafeGetContext
    val mappedTasks = tasks.asScala.map(c => new WrappedCallable[T](c, context)).asJavaCollection
    service.invokeAll(mappedTasks, timeout, unit)
  }

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T = {
    val context = unsafeGetContext
    val mappedTasks = tasks.asScala.map(c => new WrappedCallable[T](c, context)).asJavaCollection
    service.invokeAny(mappedTasks)
  }

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = {
    val context = unsafeGetContext
    val mappedTasks = tasks.asScala.map(c => new WrappedCallable[T](c, context)).asJavaCollection
    service.invokeAny(mappedTasks, timeout, unit)
  }

  override def execute(command: Runnable): Unit = {
    service.execute(
      new WrappedRunnable(command, unsafeGetContext)
    )
  }
}

class WrappedRunnable(runnable: Runnable, context: LoggingContext) extends Runnable {
  override def run(): Unit = {
    LoggingContext.localContext.value = context
    runnable.run()
  }
}

class WrappedCallable[A](callable: Callable[A], context: LoggingContext) extends Callable[A] {
  override def call(): A = {
    LoggingContext.localContext.value = context
    callable.call()
  }
}
