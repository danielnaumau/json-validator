package com.snowplow.http.dsl

import cats.data.NonEmptyList
import cats.effect.kernel.Async
import cats.implicits._
import com.snowplow.SchemaId
import com.snowplow.http.Models.{Action, Response, Status}
import com.snowplow.stores.SchemasStore
import io.circe.Json
import io.circe.schema.{Schema, ValidationError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, Response => HttpResponse}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._

final class ValidationsDsl[F[_]: Async: Logger](schemasStore: SchemasStore[F]) extends Http4sDsl[F] {
  def routes(implicit responseEncoder: EntityEncoder[F, Response]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / schemaId =>
        val res = for {
          json      <- req.as[Json]
          schemaOpt <- schemasStore.get(schemaId).map(_.map(Schema.load))
          status <- schemaOpt
                     .map(schema => validateJson(schemaId, schema, json))
                     .getOrElse(
                       warn"Failed to validate scheme: $schemaId doesn't exist" *> NotFound(notFound(schemaId))
                     )
        } yield status

        res.handleErrorWith(
          error =>
            warn"Failed to validate scheme $schemaId: ${error.getLocalizedMessage}" *>
              BadRequest(errorResponse(schemaId, error.getLocalizedMessage))
        )

    }

  private def notFound(schemaId: SchemaId): Response =
    Response(schemaId, Action.ValidateDocument, Status.Error, s"Schema: $schemaId doesn't exist".some)

  private def errorResponse(schemaId: SchemaId, msg: String): Response =
    Response(schemaId, Action.ValidateDocument, Status.Error, Some(msg))

  private def validateJson(
      schemaId: SchemaId,
      schema: Schema,
      json: Json
  )(implicit responseEncoder: EntityEncoder[F, Response]): F[HttpResponse[F]] =
    schema
      .validate(json.deepDropNullValues)
      .fold(
        errors => BadRequest(errorResponse(schemaId, allErrors(errors))),
        _ => Ok(Response(schemaId, Action.ValidateDocument, Status.Success))
      )

  private def allErrors(errors: NonEmptyList[ValidationError]): String =
    errors.map(_.getLocalizedMessage).mkString_(" ")
}
