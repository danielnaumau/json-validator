import sbt._

object Dependencies {

  object V {
    val http4sVersion      = "0.23.12"
    val circeVersion       = "0.14.1"
    val scalaTestVersion   = "3.2.11"
    val circeSchemaVersion = "0.2.0"
    val redisVersion       = "1.2.0"
    val pureConfigVersion  = "0.17.1"

    val enumeratumCirceVersion = "1.7.0"
    val mockitoVersion         = "3.2.12.0"

    val log4jVersion   = "2.2.0"
    val logbackVersion = "1.2.10"

  }

  object Libraries {
    val http4sDsl         = "org.http4s" %% "http4s-dsl"          % V.http4sVersion
    val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % V.http4sVersion
    val http4sCirce       = "org.http4s" %% "http4s-circe"        % V.http4sVersion

    val scalaTest = "org.scalatest"     %% "scalatest"   % V.scalaTestVersion % Test
    val mockito   = "org.scalatestplus" %% "mockito-4-5" % V.mockitoVersion   % Test

    val circeCore    = "io.circe" %% "circe-core"           % V.circeVersion
    val circeGeneric = "io.circe" %% "circe-generic"        % V.circeVersion
    val circeParser  = "io.circe" %% "circe-parser"         % V.circeVersion
    val circeSchema  = "io.circe" %% "circe-json-schema"    % V.circeSchemaVersion

    val redis           = "dev.profunktor"        %% "redis4cats-effects" % V.redisVersion
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"         % V.pureConfigVersion
    val enumeratumCirce = "com.beachape"          %% "enumeratum-circe"   % V.enumeratumCirceVersion

    val slf4jCats = "org.typelevel" %% "log4cats-slf4j"  % V.log4jVersion
    val logback   = "ch.qos.logback" % "logback-classic" % V.logbackVersion
  }

}
