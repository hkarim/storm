package storm.event

import io.circe.*
import io.circe.syntax.*

sealed trait MessageBody {
  def tpe: String

  def messageId: Option[Int]

  def inReplyTo: Option[Int]
}

sealed trait RequestBody extends MessageBody

object RequestBody {
  object Type {
    final val InitializationRequest: String = "init"
  }

  case class InitializationRequest(
    messageId: Option[Int],
    inReplyTo: Option[Int],
    nodeId: String,
    nodeIds: List[String],
  ) extends RequestBody {
    override final val tpe: String = Type.InitializationRequest
  }

  object InitializationRequest {
    given Decoder[InitializationRequest] = for {
      messageId <- Decoder[Option[Int]].at("message_id")
      inReplyTo <- Decoder[Option[Int]].at("in_reply_to")
      nodeId <- Decoder[String].at("node_id")
      nodeIds <- Decoder[List[String]].at("node_ids")
    } yield InitializationRequest(
      messageId = messageId,
      inReplyTo = inReplyTo,
      nodeId = nodeId,
      nodeIds = nodeIds
    )
  }

}

sealed trait ResponseBody extends MessageBody

object ResponseBody {

  object Type {
    final val InitializationResponse: String = "init_ok"
    final val ErrorResponse: String = "error"
  }

  case class InitializationResponse(
    messageId: Option[Int],
    inReplyTo: Option[Int],
  ) extends ResponseBody {
    override final val tpe: String = Type.InitializationResponse
  }

  object InitializationResponse {
    given Encoder[InitializationResponse] = Encoder.instance { v =>
      Json.obj(
        "message_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson
      )
    }
  }

  case class ErrorResponse(
    messageId: Option[Int],
    inReplyTo: Option[Int],
    code: Int,
    text: Option[String],
  ) extends ResponseBody {
    override final val tpe: String = Type.ErrorResponse
  }

  object ErrorResponse {
    given Encoder[ErrorResponse] = Encoder.instance { v =>
      Json.obj(
        "message_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "code" -> v.code.asJson,
        "text" -> v.code.asJson,
      )
    }
  }
}
