package com.snowplow.http

import cats.effect._
import io.circe.Json
import org.http4s.{Method, Request, Uri}
import org.http4s.circe._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HttpServerSpec extends AnyFlatSpec with HttpServerFixtures with Matchers {
  it should "download the scheme if it exists" in {
    val request = Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString("/schema/existing-scheme")
    )

    val response = sendRequest(request)

    response.status.code shouldBe 200
  }

  it should "return 404 if the scheme doesn't exist" in {
    val request = Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString("/schema/non-existing-scheme")
    )

    val response = sendRequest(request)

    response.status.code shouldBe 404
  }

  it should "return 201 if the scheme can be uploaded" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/schema/non-uploaded-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 201
  }

  it should "return 409 if the scheme already exists" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/schema/uploaded-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 409
  }

  it should "return 200 if the json can be validated" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/validate/existing-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 200
  }
}
