name := "logging-context"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq(
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:dynamics"
)

val catsVersion       = "2.1.0"
val catsEffectVersion = "2.1.1"
val catsLoggerVersion = "1.0.1"
val logbackVersion    = "1.2.3"
val aspectVersion     = "1.9.5"

libraryDependencies ++= {
  Seq(
    "io.chrisdavenport" %% "log4cats-slf4j" % catsLoggerVersion,
    "ch.qos.logback"    % "logback-classic" % logbackVersion,
    "org.typelevel"     %% "cats-core"      % catsVersion,
    "org.typelevel"     %% "cats-effect"    % catsEffectVersion,
    "org.aspectj"       % "aspectjweaver"   % aspectVersion,
    "org.aspectj"       % "aspectjrt"       % aspectVersion,
  )
}

def getWeaver = Def.task {
  update.value.matching {
    moduleFilter(organization = "org.aspectj", name = "aspectjweaver") &&
    artifactFilter(`type` = "jar")
  }.headOption
}

javaOptions in run ++= {
  getWeaver.value.toSeq.map("-javaagent:" + _)
}

fork in run := true
