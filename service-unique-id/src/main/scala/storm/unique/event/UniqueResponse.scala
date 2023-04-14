package storm.unique.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type UniqueResponse = Response[UniqueResponseBody]

case class UniqueResponseBody(
  messageId: Long,
  inReplyTo: Long,
  id: String
) extends ResponseBody {
  override final val tpe: String = "generate_ok"
}

object UniqueResponseBody {

  given Encoder[UniqueResponseBody] = Encoder.instance[UniqueResponseBody] { v =>
    Json.obj(
      "type"        -> v.tpe.asJson,
      "in_reply_to" -> v.inReplyTo.asJson,
      "id"          -> v.id.asJson,
    )
  }

}
