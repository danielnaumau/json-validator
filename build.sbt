import Dependencies._

ThisBuild / version := "latest"
ThisBuild / scalaVersion := "2.13.8"

enablePlugins(JavaAppPackaging)


resolvers += "jitpack".at("https://jitpack.io")
dockerRepository := Some("danielnaumau")

lazy val root = (project in file("."))
  .settings(
    name := "json-validator",
    libraryDependencies ++= Seq(
      Libraries.circeSchema,
      Libraries.circeCore,
      Libraries.circeParser,
      Libraries.circeGeneric,
      Libraries.http4sBlazeServer,
      Libraries.http4sDsl,
      Libraries.http4sCirce,
      Libraries.redis,
      Libraries.pureConfig,
      Libraries.enumeratumCirce,
      Libraries.scalaTest,
      Libraries.mockito,
      Libraries.logback,
      Libraries.slf4jCats,
    )
  )
