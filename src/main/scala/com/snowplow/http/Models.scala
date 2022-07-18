package com.snowplow.http

import com.snowplow.SchemaId
import enumeratum._

object Models {

  final case class Response(id: SchemaId, action: Action, status: Status, message: Option[String] = None)

  sealed trait Action extends EnumEntry

  case object Action extends Enum[Action] with CirceEnum[Action] {
    case object UploadSchema     extends Action
    case object ValidateDocument extends Action
    case object DownloadSchema   extends Action

    val values = findValues
  }

  sealed trait Status extends EnumEntry

  case object Status extends Enum[Status] with CirceEnum[Status] {
    case object Error   extends Status
    case object Success extends Status

    val values = findValues
  }
}
