package com.snowplow.stores

import com.snowplow.SchemaId
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import dev.profunktor.redis4cats.data.RedisCodec
import io.circe.Json
import io.circe.jawn._

object RedisCodecs {
  val schemaIdEpi: SplitEpi[String, SchemaId] =
    SplitEpi(SchemaId.apply, _.value)

  val jsonEpi: SplitEpi[String, Json] =
    SplitEpi(parse(_).getOrElse(Json.obj()), _.noSpaces)

  val schemasStoreCodec: RedisCodec[SchemaId, Json] =
    Codecs.derive(RedisCodec.Utf8, schemaIdEpi, jsonEpi)
}
