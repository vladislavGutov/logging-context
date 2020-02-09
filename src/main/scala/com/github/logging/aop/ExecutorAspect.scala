package com.github.logging.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}

@Aspect
class ExecutorAspect extends ContextPopulationWrapper {

  @Pointcut(value = "call( * java.util.concurrent.Executor+.execute(Runnable))")
  def executePointcut(): Unit = ()

  @Around(value = "executePointcut()", argNames = "jp")
  def executeHandle(jp: ProceedingJoinPoint): Object = {
    handleRunnableArgument(jp)
  }

}
