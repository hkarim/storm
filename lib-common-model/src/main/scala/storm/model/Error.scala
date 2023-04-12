package storm.model

import io.circe.*
import io.circe.syntax.*

case class ErrorResponseBody(
  messageId: Long,
  inReplyTo: Long,
  code: Int,
  text: Option[String]
) extends ResponseBody {
  override final val tpe: String = "error"
}

object ErrorResponseBody {
  given Encoder[ErrorResponseBody] =
    Encoder.instance[ErrorResponseBody] { v =>
      Json.obj(
        "type"        -> v.tpe.asJson,
        "msg_id"      -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "code"        -> v.code.asJson,
        "text"        -> v.text.asJson,
      )
    }
}

type ErrorResponse = Response[ErrorResponseBody]
