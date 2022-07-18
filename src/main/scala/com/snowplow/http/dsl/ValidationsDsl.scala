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
import org.http4s.{EntityEncoder, HttpRoutes}

final class ValidationsDsl[F[_]: Async](schemasStore: SchemasStore[F]) extends Http4sDsl[F] {
  def routes(implicit responseEncoder: EntityEncoder[F, Response]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / schemaId =>
        val res = for {
          json <- req.as[Json]
          schemaOpt <- schemasStore.get(schemaId).map(_.map(Schema.load))
          status <- schemaOpt
            .map(schema => Ok(validateJson(schemaId, schema, json)))
            .getOrElse(NotFound(notFound(schemaId)))
        } yield status

        res.handleErrorWith(error => BadRequest(errorResponse(schemaId, error.getLocalizedMessage)))

    }

  private def notFound(schemaId: SchemaId): Response =
    Response(schemaId, Action.ValidateDocument, Status.Error, s"Schema: $schemaId doesn't exist".some)

  private def errorResponse(schemaId: SchemaId, msg: String): Response =
    Response(schemaId, Action.ValidateDocument, Status.Error, Some(msg))

  private def allErrors(errors: NonEmptyList[ValidationError]): String =
    errors.map(_.getLocalizedMessage).mkString_(" ")

  private def validateJson(schemaId: SchemaId, schema: Schema, json: Json): Response =
    schema
      .validate(json.deepDropNullValues)
      .fold(
        errors => errorResponse(schemaId, allErrors(errors)),
        _ => Response(schemaId, Action.ValidateDocument, Status.Success)
      )
}
