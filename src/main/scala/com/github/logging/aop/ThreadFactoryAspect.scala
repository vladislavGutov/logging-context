package com.github.logging.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}

@Aspect
class ThreadFactoryAspect extends ContextPopulationWrapper {

  @Pointcut(value = "call( * java.util.concurrent.ThreadFactory+.newThread(Runnable))")
  def factoryNewThreadPointcut(): Unit = ()

  @Around(value = "factoryNewThreadPointcut()", argNames = "jp")
  def factoryNewThreadHandle(jp: ProceedingJoinPoint): Object = {
    handleRunnableArgument(jp)
  }

}
