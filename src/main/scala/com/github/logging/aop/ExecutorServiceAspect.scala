package com.github.logging.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}

@Aspect
class ExecutorServiceAspect extends ContextPopulationWrapper {

  @Pointcut(value = "call( * java.util.concurrent.ExecutorService+.submit(java.util.concurrent.Callable))")
  def submitCallablePointcut(): Unit = ()

  @Pointcut(value = "call( * java.util.concurrent.ExecutorService+.submit(Runnable, ..))")
  def submitRunnablePointcut(): Unit = ()

  @Pointcut(value = "call( * java.util.concurrent.ExecutorService+.invoke*(java.util.Collection, ..))")
  def invokePointcut(): Unit = ()

  @Around(value = "submitCallablePointcut()", argNames = "jp")
  def submitCallableHandle(jp: ProceedingJoinPoint): Object = handleCallableArgument(jp)

  @Around(value = "submitRunnablePointcut()", argNames = "jp")
  def submitRunnableHandle(jp: ProceedingJoinPoint): Object = handleRunnableArgument(jp)

  @Around(value = "invokePointcut()", argNames = "jp")
  def invokeAllHandle(jp: ProceedingJoinPoint): Object = handleCallableCollectionArgument(jp)

}
