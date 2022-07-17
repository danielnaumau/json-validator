package com.snowplow.http

import cats.effect.{Async, ExitCode}
import cats.implicits.toFunctorOps
import com.snowplow.AppConfig.HttpConfig
import com.snowplow.stores.SchemasStore
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.syntax.all._

class HttpServer[F[_]: Async](routes: HttpRoutes[F], httpConfig: HttpConfig) {
  def start: F[ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

object HttpServer {
  def apply[F[_]: Async](httpConfig: HttpConfig, schemasStore: SchemasStore[F]): HttpServer[F] = {
    val schemasDsl     = new SchemasDsl[F](schemasStore)
    val healthCheckDsl = new HealthCheckDsl[F]()

    val routes = Router[F](
      "isAlive" -> healthCheckDsl.routes,
      "schema" -> schemasDsl.routes
    )

    new HttpServer[F](routes, httpConfig)
  }
}
