package com.snowplow

import cats.effect.{ExitCode, IO, IOApp}
import com.snowplow.http.HttpServer
import com.snowplow.stores.SchemasStore
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import org.typelevel.log4cats.SelfAwareLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {
  implicit val catsLogger: SelfAwareLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- AppConfig.load[IO]
      _      <- catsLogger.info(s"Loaded config: $config")
      exitCode <- SchemasStore
                   .make[IO](config.redis.uri)
                   .use(HttpServer(config.http, _).start)
    } yield exitCode
  }
}
