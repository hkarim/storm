package storm.unique.event

import io.circe.*
import storm.model.*

type UniqueRequest = Request[UniqueRequestBody]

case class UniqueRequestBody(
  messageId: Long,
) extends RequestBody {
  override final val tpe: String = "generate"
}

object UniqueRequestBody {
  given Decoder[UniqueRequestBody] =
    for {
      messageId <- Decoder[Long].at("msg_id")
    } yield UniqueRequestBody(
      messageId = messageId,
    )
}
