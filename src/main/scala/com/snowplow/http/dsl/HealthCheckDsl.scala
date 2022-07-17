package com.snowplow.http.dsl

import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class HealthCheckDsl[F[_]: Async]() extends Http4sDsl[F] {
  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root => Ok("alive")
    }
}
