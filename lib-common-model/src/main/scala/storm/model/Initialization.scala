package storm.model

import io.circe.*
import io.circe.syntax.*

case class InitializationRequestBody(
  messageId: Long,
  nodeId: String,
  nodeIds: List[String],
) extends RequestBody {
  override final val tpe: String = "init"
}

object InitializationRequestBody {
  given Decoder[InitializationRequestBody] =
    for {
      messageId <- Decoder[Long].at("msg_id")
      nodeId    <- Decoder[String].at("node_id")
      nodeIds   <- Decoder[List[String]].at("node_ids")
    } yield InitializationRequestBody(
      messageId = messageId,
      nodeId = nodeId,
      nodeIds = nodeIds,
    )
}

case class InitializationResponseBody(
  messageId: Long,
  inReplyTo: Long,
) extends ResponseBody {
  override final val tpe: String = "init_ok"
}

object InitializationResponseBody {
  given Encoder[InitializationResponseBody] =
    Encoder.instance[InitializationResponseBody] { v =>
      Json.obj(
        "type"        -> v.tpe.asJson,
        "msg_id"      -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
      )
    }
}

type InitializationRequest  = Request[InitializationRequestBody]
type InitializationResponse = Response[InitializationResponseBody]
