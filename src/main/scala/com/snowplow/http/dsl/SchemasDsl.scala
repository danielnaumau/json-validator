package com.snowplow.http.dsl

import cats.effect.kernel.Async
import cats.implicits._
import com.snowplow.SchemaId
import com.snowplow.http.Models._
import com.snowplow.stores.SchemasStore
import io.circe._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.{EntityEncoder, HttpRoutes}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._

final class SchemasDsl[F[_]: Async: Logger](schemasStore: SchemasStore[F]) extends Http4sDsl[F] {

  def routes(implicit responseEncoder: EntityEncoder[F, Response]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / schemaId =>
        schemasStore
          .get(schemaId)
          .flatMap(_.map(Ok(_)).getOrElse(NotFound(notFound(schemaId))))
      case req @ POST -> Root / schemaId =>
        val res = for {
          json <- req.as[Json]
          res  <- schemasStore.add(schemaId, json)
          status <- if (res)
                     Created(Response(schemaId, Action.UploadSchema, Status.Success))
                   else {
                     warn"Failed to post scheme $schemaId: $schemaId already exists" *>
                       Conflict(errorResponse(schemaId, s"$schemaId already exists"))
                   }
        } yield status

        res
          .handleErrorWith(
            error =>
              warn"Failed to post scheme $schemaId: ${error.getMessage}" *>
                BadRequest(errorResponse(schemaId, error.getLocalizedMessage))
          )

    }

  private def errorResponse(schemaId: SchemaId, msg: String): Response =
    Response(schemaId, Action.UploadSchema, Status.Error, Some(msg))

  private def notFound(schemaId: SchemaId): Response =
    Response(schemaId, Action.DownloadSchema, Status.Error, s"Schema $schemaId doesn't exist".some)
}
