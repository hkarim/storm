package storm.event

import io.circe.*
import io.circe.syntax.*

sealed trait MessageBody {
  def tpe: String

  def messageId: Option[Int]

}

sealed trait RequestBody extends MessageBody

object RequestBody {
  object Type {
    final val InitializationRequest: String = "init"
    final val EchoRequest: String = "echo"
  }

  case class InitializationRequest(
    messageId: Option[Int],
    nodeId: String,
    nodeIds: List[String],
  ) extends RequestBody {
    override final val tpe: String = Type.InitializationRequest
  }

  object InitializationRequest {
    given Decoder[InitializationRequest] = for {
      messageId <- Decoder[Option[Int]].at("msg_id")
      nodeId <- Decoder[String].at("node_id")
      nodeIds <- Decoder[List[String]].at("node_ids")
    } yield InitializationRequest(
      messageId = messageId,
      nodeId = nodeId,
      nodeIds = nodeIds
    )
  }

  case class EchoRequest(
    messageId: Option[Int],
    echo: String,
  ) extends RequestBody {
    override final val tpe: String = Type.EchoRequest
  }

  object EchoRequest {
    given Decoder[EchoRequest] = for {
      messageId <- Decoder[Option[Int]].at("msg_id")
      echo <- Decoder[String].at("echo")
    } yield EchoRequest(
      messageId = messageId,
      echo = echo,
    )
  }

}

sealed trait ResponseBody extends MessageBody

object ResponseBody {

  object Type {
    final val InitializationResponse: String = "init_ok"
    final val EchoResponse: String = "echo_ok"
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
        "type" -> v.tpe.asJson,
        "message_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson
      )
    }
  }

  case class EchoResponse(
    messageId: Option[Int],
    inReplyTo: Option[Int],
    echo: String
  ) extends ResponseBody {
    override final val tpe: String = Type.EchoResponse
  }

  object EchoResponse {
    given Encoder[EchoResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "message_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "echo" -> v.echo.asJson,
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
        "type" -> v.tpe.asJson,
        "message_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "code" -> v.code.asJson,
        "text" -> v.text.asJson,
      )
    }
  }
}
