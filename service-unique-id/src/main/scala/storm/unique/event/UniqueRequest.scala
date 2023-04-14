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
      messageId <- Decoders.messageId
    } yield UniqueRequestBody(
      messageId = messageId,
    )
}
