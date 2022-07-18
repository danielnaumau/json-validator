package com.snowplow.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.snowplow.AppConfig.HttpConfig
import com.snowplow.http.Models.{Response, Status}
import com.snowplow.stores.SchemasStore
import io.circe._
import io.circe.generic.auto._
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, Request, Response => HttpResponse}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.typelevel.log4cats.SelfAwareLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait HttpServerFixtures {
  private val stringScheme =
    """
          |{
          |  "type": "object",
          |  "properties": {
          |    "source": {
          |      "type": "string"
          |    },
          |    "destination": {
          |      "type": "string"
          |    },
          |    "timeout": {
          |      "type": "integer",
          |      "minimum": 0,
          |      "maximum": 32767
          |    },
          |    "chunks": {
          |      "type": "object",
          |      "properties": {
          |        "size": {
          |          "type": "integer"
          |        },
          |        "number": {
          |          "type": "integer"
          |        }
          |      },
          |      "required": ["size"]
          |    }
          |  },
          |  "required": ["source", "destination"]
          |}
          |""".stripMargin

  private val validationJsonString =
    """
      |{
      |  "source": "/home/alice/image.iso",
      |  "destination": "/mnt/storage",
      |  "timeout": null,
      |  "chunks": {
      |    "size": 1024,
      |    "number": null
      |  }
      |}
      |""".stripMargin

  val jsonScheme     = jawn.parse(stringScheme).toOption
  val validationJson = jawn.parse(validationJsonString).toOption.get

  val schemasStore = mock[SchemasStore[IO]]
  when(schemasStore.get("existing-scheme")).thenReturn(IO { jsonScheme })
  when(schemasStore.get("non-existing-scheme")).thenReturn(IO { None })
  when(schemasStore.add("non-uploaded-scheme", Json.obj())).thenReturn(IO { true })
  when(schemasStore.add("uploaded-scheme", Json.obj())).thenReturn(IO { false })

  implicit val catsLogger: SelfAwareLogger[IO] = Slf4jLogger.getLogger[IO]
  val httpServer = HttpServer(HttpConfig("0.0.0.0", 8080), schemasStore)

  def sendRequest(request: Request[IO]): HttpResponse[IO] =
    httpServer.routes(request).value.unsafeRunSync().get

  def status(httpResponse: HttpResponse[IO]): Status =
    httpResponse.as[Response].unsafeRunSync().status

  implicit val responseDecoder: EntityDecoder[IO, Response] = jsonOf[IO, Response]
}
