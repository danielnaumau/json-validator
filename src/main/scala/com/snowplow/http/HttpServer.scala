package com.snowplow.http

import io.circe.generic.auto._
import cats.effect.{Async, ExitCode}
import cats.implicits.toFunctorOps
import com.snowplow.AppConfig.HttpConfig
import com.snowplow.http.Models.Response
import com.snowplow.http.dsl.{HealthCheckDsl, SchemasDsl, ValidationsDsl}
import com.snowplow.stores.SchemasStore
import org.typelevel.log4cats.Logger
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.jsonEncoderOf
import org.http4s.server.Router
import org.http4s.syntax.all._

final case class HttpServer[F[_]: Async](routes: HttpRoutes[F], httpConfig: HttpConfig) {
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
  def apply[F[_]: Async: Logger](httpConfig: HttpConfig, schemasStore: SchemasStore[F]): HttpServer[F] = {
    implicit val responseEntityEncoder: EntityEncoder[F, Response] = jsonEncoderOf[F, Response]

    val schemasDsl     = new SchemasDsl[F](schemasStore)
    val validationsDsl = new ValidationsDsl[F](schemasStore)
    val healthCheckDsl = new HealthCheckDsl[F]

    val routes = Router[F](
      "isAlive" -> healthCheckDsl.routes,
      "schema" -> schemasDsl.routes,
      "validate" -> validationsDsl.routes
    )

    new HttpServer[F](routes, httpConfig)
  }
}
