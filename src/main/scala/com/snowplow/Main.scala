package com.snowplow

import cats.effect.{ExitCode, IO, IOApp}
import com.snowplow.http.HttpServer
import com.snowplow.stores.SchemasStore
import dev.profunktor.redis4cats.effect.Log.NoOp.instance

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config   <- AppConfig.load[IO]
      exitCode <- SchemasStore
                   .make[IO](config.redis.uri)
                   .use(HttpServer(config.http, _).start)
    } yield exitCode
  }
}
