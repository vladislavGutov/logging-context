name := "logging-context"

version := "0.1"

lazy val v = "2.13.1"

scalaVersion := v


lazy val scalacSettings = Seq(
  "-Ypartial-unification",
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:dynamics"
)

scalacOptions ++= scalacSettings

addCompilerPlugin("org.typelevel" % s"kind-projector_$v" % "0.11.0")

val catsVersion = "2.1.0"
val catsEffectVersion = "2.1.1"
val catsLoggerVersion = "1.0.1"
val logbackVersion = "1.2.3"

libraryDependencies ++= {
  Seq(
    "io.chrisdavenport" %% "log4cats-slf4j" % catsLoggerVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion
  )
}