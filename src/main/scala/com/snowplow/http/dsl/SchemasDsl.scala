package com.snowplow.http.dsl

import cats.effect.kernel.Async
import cats.implicits._
import com.snowplow.SchemaId
import com.snowplow.http.Models._
import com.snowplow.stores.SchemasStore
import io.circe.Json
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.{EntityEncoder, HttpRoutes}

final class SchemasDsl[F[_]: Async](schemasStore: SchemasStore[F]) extends Http4sDsl[F] {

  def routes(implicit responseEncoder: EntityEncoder[F, Response]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / schemaId =>
        schemasStore
          .get(schemaId)
          .flatMap(_.map(Ok(_)).getOrElse(NotFound(notFound(schemaId))))
      case req @ POST -> Root / schemaId =>
        for {
          json <- req.as[Json]
          res  <- schemasStore.add(schemaId, json)
          status <-
            if (res)
              Created(Response(schemaId, Action.UploadSchema, Status.Success))
            else
              Conflict(Response(schemaId, Action.UploadSchema, Status.Error, Some(s"$schemaId already exists")))
        } yield status
    }

  private def notFound(schemaId: SchemaId): Response =
    Response(schemaId, Action.DownloadSchema, Status.Error, s"Schema $schemaId doesn't exist".some)
}
