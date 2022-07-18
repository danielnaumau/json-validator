package com.snowplow.http

import com.snowplow.SchemaId
import enumeratum._

object Models {

  sealed abstract class Action(override val entryName: String) extends EnumEntry

  sealed abstract class Status(override val entryName: String) extends EnumEntry

  final case class Response(id: SchemaId, action: Action, status: Status, message: Option[String] = None)

  case object Action extends Enum[Action] with CirceEnum[Action] {
    val values = findValues

    case object UploadSchema     extends Action("uploadSchema")

    case object ValidateDocument extends Action("validateDocument")

    case object DownloadSchema   extends Action("uploadSchema")
  }

  case object Status extends Enum[Status] with CirceEnum[Status] {
    val values = findValues

    case object Error   extends Status("error")

    case object Success extends Status("success")
  }
}
