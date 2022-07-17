package com.snowplow

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import com.snowplow.AppConfig.{HttpConfig, RedisConfig}
import pureconfig._
import pureconfig.generic.auto._


final case class AppConfig(
    redis: RedisConfig,
    http: HttpConfig
)

object AppConfig {
  final case class RedisConfig(uri: String)
  final case class HttpConfig(host: String, port: Int)

  def load[F[_]: Sync]: F[AppConfig] = ConfigSource.default.loadOrThrow[AppConfig].pure[F]
}
