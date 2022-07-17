package com.snowplow.stores

import cats.effect.{Async, Resource}
import com.snowplow.SchemaId
import com.snowplow.stores.RedisCodecs.schemasStoreCodec
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import io.circe.Json

sealed trait SchemasStore[F[_]] {
  def get(schemaId: SchemaId): F[Option[Json]]
  def add(schemaId: SchemaId, schema: Json): F[Unit]
}

object SchemasStore {
  def make[F[_]: Async: Log](url: String): Resource[F, SchemasStore[F]] = {
    Redis[F]
      .simple(url, schemasStoreCodec)
      .map(new SchemasStoreImpl[F](_))
  }

  private final class SchemasStoreImpl[F[_]](redis: RedisCommands[F, SchemaId, Json]) extends SchemasStore[F] {
    override def get(schemaId: SchemaId): F[Option[Json]] = {
      redis.get(schemaId)
    }

    override def add(schemaId: SchemaId, schema: Json): F[Unit] = {
      redis.set(schemaId, schema)
    }
  }
}