package com.snowplow.http

import com.snowplow.SchemaId
import enumeratum._

object Models {

  final case class Response(id: SchemaId, action: Action, status: Status, message: Option[String] = None)

  sealed abstract class Action(override val entryName: String) extends EnumEntry

  case object Action extends Enum[Action] with CirceEnum[Action] {
    case object UploadSchema     extends Action("uploadSchema")
    case object ValidateDocument extends Action("validateDocument")
    case object DownloadSchema   extends Action("uploadSchema")

    val values = findValues
  }

  sealed abstract class Status(override val entryName: String) extends EnumEntry

  case object Status extends Enum[Status] with CirceEnum[Status] {
    case object Error   extends Status("error")
    case object Success extends Status("success")

    val values = findValues
  }
}
