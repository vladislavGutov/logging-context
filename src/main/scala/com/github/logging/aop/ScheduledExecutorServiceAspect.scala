package com.github.logging.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}

@Aspect
class ScheduledExecutorServiceAspect extends ContextPopulationWrapper {

  @Pointcut(value = "call( * java.util.concurrent.ScheduledExecutorService+.schedule*(Runnable, ..))")
  def scheduleRunnablePointcut(): Unit = ()

  @Pointcut(value = "call( * java.util.concurrent.ScheduledExecutorService+.schedule(java.util.concurrent.Callable, ..))")
  def scheduleCallablePointcut(): Unit = ()

  @Around(value = "scheduleRunnablePointcut()", argNames = "jp")
  def scheduleRunnableHandle(jp: ProceedingJoinPoint): Object = {
    handleRunnableArgument(jp)
  }

  @Around(value = "scheduleCallablePointcut()", argNames = "jp")
  def scheduleCallableHandle(jp: ProceedingJoinPoint): Object = {
    handleCallableArgument(jp)
  }

}
