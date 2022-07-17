package com.snowplow.http

import cats.effect.kernel.Async
import cats.implicits._
import com.snowplow.SchemaId
import io.circe.Json
import com.snowplow.stores.SchemasStore
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl._


final class SchemasDsl[F[_]: Async](schemasStore: SchemasStore[F]) extends Http4sDsl[F] {
  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / schemaId =>
        schemasStore.get(SchemaId(schemaId)).flatMap(_.map(Ok(_)).getOrElse(NotFound()))
      case req @ POST -> Root / schemaId =>
        for {
          json <- req.as[Json]
          _    <- schemasStore.add(SchemaId(schemaId), json) // to be fixed
          res  <- Ok()
        } yield res
    }
}
