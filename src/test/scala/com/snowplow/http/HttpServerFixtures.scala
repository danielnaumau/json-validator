package com.snowplow.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.snowplow.AppConfig.HttpConfig
import com.snowplow.stores.SchemasStore
import io.circe._
import org.http4s.{Request, Response => HttpResponse}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock

trait HttpServerFixtures {
  val stringScheme =
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

  val jsonScheme = jawn.parse(stringScheme).toOption

  val schemasStore = mock[SchemasStore[IO]]
  when(schemasStore.get("existing-scheme")).thenReturn(IO { jsonScheme })
  when(schemasStore.get("non-existing-scheme")).thenReturn(IO { None })
  when(schemasStore.add("non-uploaded-scheme", Json.obj())).thenReturn(IO { true })
  when(schemasStore.add("uploaded-scheme", Json.obj())).thenReturn(IO { false })


  val httpServer = HttpServer(HttpConfig("0.0.0.0", 8080), schemasStore)

  def sendRequest(request: Request[IO]): HttpResponse[IO] =
    httpServer.routes(request).value.unsafeRunSync().get
}
