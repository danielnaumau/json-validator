package com.snowplow.http

import cats.effect._
import com.snowplow.http.Models.Status
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

  it should "return 404 and error msg if the scheme doesn't exist" in {
    val request = Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString("/schema/non-existing-scheme")
    )

    val response = sendRequest(request)

    response.status.code shouldBe 404
    status(response) shouldBe Status.Error
  }

  it should "return status code 201 and success msg if the scheme can be uploaded" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/schema/non-uploaded-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 201
    status(response) shouldBe Status.Success
  }

  it should "return status code 409 and error msg if the scheme already exists" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/schema/uploaded-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 409
    status(response) shouldBe Status.Error
  }

  it should "return status code 400 and error msg if the json wasn't validated" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/validate/existing-scheme"),
    ).withEntity[Json](Json.obj())

    val response = sendRequest(request)

    response.status.code shouldBe 400
    status(response) shouldBe Status.Error
  }

  it should "return status code 200 and success msg if the json was validated" in {
    val request = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/validate/existing-scheme"),
    ).withEntity[Json](validationJson)

    val response = sendRequest(request)

    response.status.code shouldBe 200
    status(response) shouldBe Status.Success
  }
}

