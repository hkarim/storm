package storm.event

import io.circe.*
import io.circe.syntax.*

sealed trait MessageBody {
  def tpe: String

  def messageId: Option[Long]

}

sealed trait RequestBody extends MessageBody

object RequestBody {
  object Type {
    final val InitializationRequest: String = "init"
    final val EchoRequest: String = "echo"
    final val BroadcastRequest: String = "broadcast"
    final val ReadRequest: String = "read"
    final val TopologyRequest: String = "topology"
  }

  /// Initialization

  case class InitializationRequest(
    messageId: Option[Long],
    nodeId: String,
    nodeIds: List[String],
  ) extends RequestBody {
    override final val tpe: String = Type.InitializationRequest
  }

  object InitializationRequest {
    given Decoder[InitializationRequest] = for {
      messageId <- Decoder[Option[Long]].at("msg_id")
      nodeId <- Decoder[String].at("node_id")
      nodeIds <- Decoder[List[String]].at("node_ids")
    } yield InitializationRequest(
      messageId = messageId,
      nodeId = nodeId,
      nodeIds = nodeIds
    )
  }

  /// Echo

  case class EchoRequest(
    messageId: Option[Long],
    echo: String,
  ) extends RequestBody {
    override final val tpe: String = Type.EchoRequest
  }

  object EchoRequest {
    given Decoder[EchoRequest] = for {
      messageId <- Decoder[Option[Long]].at("msg_id")
      echo <- Decoder[String].at("echo")
    } yield EchoRequest(
      messageId = messageId,
      echo = echo,
    )
  }

  /// Broadcast

  case class BroadcastRequest(
    messageId: Option[Long],
    message: Long
  ) extends RequestBody {
    override final val tpe: String = Type.BroadcastRequest
  }

  object BroadcastRequest {
    given Decoder[BroadcastRequest] = for {
      messageId <- Decoder[Option[Long]].at("msg_id")
      message <- Decoder[Long].at("message")
    } yield BroadcastRequest(
      messageId = messageId,
      message = message,
    )
  }

  /// Read

  case class ReadRequest(
    messageId: Option[Long],
  ) extends RequestBody {
    override final val tpe: String = Type.ReadRequest
  }

  object ReadRequest {
    given Decoder[ReadRequest] = for {
      messageId <- Decoder[Option[Long]].at("msg_id")
    } yield ReadRequest(
      messageId = messageId,
    )
  }

  /// Topology

  case class TopologyRequest(
    messageId: Option[Long],
    topology: Map[String, List[String]],
  ) extends RequestBody {
    override final val tpe: String = Type.TopologyRequest
  }

  object TopologyRequest {
    given Decoder[TopologyRequest] = for {
      messageId <- Decoder[Option[Long]].at("msg_id")
      topology <- Decoder[Map[String, List[String]]].at("topology")
    } yield TopologyRequest(
      messageId = messageId,
      topology = topology,
    )
  }


}

sealed trait ResponseBody extends MessageBody

object ResponseBody {

  object Type {
    final val InitializationResponse: String = "init_ok"
    final val EchoResponse: String = "echo_ok"
    final val BroadcastResponse: String = "broadcast_ok"
    final val ReadResponse: String = "read_ok"
    final val TopologyResponse: String = "topology_ok"
    final val ErrorResponse: String = "error"
  }

  /// Initialization

  case class InitializationResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
  ) extends ResponseBody {
    override final val tpe: String = Type.InitializationResponse
  }

  object InitializationResponse {
    given Encoder[InitializationResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson
      )
    }
  }



  /// Echo

  case class EchoResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
    echo: String
  ) extends ResponseBody {
    override final val tpe: String = Type.EchoResponse
  }

  object EchoResponse {
    given Encoder[EchoResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "echo" -> v.echo.asJson,
      )
    }
  }

  /// Broadcast

  case class BroadcastResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
  ) extends ResponseBody {
    override final val tpe: String = Type.BroadcastResponse
  }

  object BroadcastResponse {
    given Encoder[BroadcastResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson
      )
    }
  }

  /// Read

  case class ReadResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
    messages: List[Long],
  ) extends ResponseBody {
    override final val tpe: String = Type.ReadResponse
  }

  object ReadResponse {
    given Encoder[ReadResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "messages" -> v.messages.asJson,
      )
    }
  }

  /// Topology

  case class TopologyResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
  ) extends ResponseBody {
    override final val tpe: String = Type.TopologyResponse
  }

  object TopologyResponse {
    given Encoder[TopologyResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
      )
    }
  }

  /// Error

  case class ErrorResponse(
    messageId: Option[Long],
    inReplyTo: Option[Long],
    code: Int,
    text: Option[String],
  ) extends ResponseBody {
    override final val tpe: String = Type.ErrorResponse
  }

  object ErrorResponse {
    given Encoder[ErrorResponse] = Encoder.instance { v =>
      Json.obj(
        "type" -> v.tpe.asJson,
        "msg_id" -> v.messageId.asJson,
        "in_reply_to" -> v.inReplyTo.asJson,
        "code" -> v.code.asJson,
        "text" -> v.text.asJson,
      )
    }
  }
}
