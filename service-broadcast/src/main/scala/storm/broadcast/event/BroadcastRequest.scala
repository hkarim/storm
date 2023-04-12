package storm.broadcast.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type BroadcastRequest = Request[BroadcastRequestBody]

sealed trait BroadcastRequestBody extends RequestBody {
  def widen: BroadcastRequestBody = this
}

object BroadcastRequestBody {

  given Decoder[BroadcastRequestBody] =
    Decoder[String].at("type").flatMap {
      case "broadcast" =>
        Decoder[Broadcast].map(_.widen)
      case "read" =>
        Decoder[Read].map(_.widen)
      case "topology" =>
        Decoder[Topology].map(_.widen)
      case "broadcast_ok" =>
        Decoder[AckBroadcast].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized broadcast request type `$otherwise`", Nil))
    }

  case class Broadcast(
    messageId: Long,
    message: Int,
  ) extends BroadcastRequestBody {
    override final val tpe: String = "broadcast"
  }

  object Broadcast {
    given Encoder[Broadcast] =
      Encoder.instance[Broadcast] { v =>
        Json.obj(
          "type"    -> v.tpe.asJson,
          "msg_id"  -> v.messageId.asJson,
          "message" -> v.message.asJson,
        )
      }

    given Decoder[Broadcast] =
      for {
        messageId <- Decoder[Long].at("msg_id")
        message   <- Decoder[Int].at("message")
      } yield Broadcast(
        messageId = messageId,
        message = message,
      )
  }

  // in multi-node broadcast workload,
  // we receive the same message as the
  // broadcast response
  case class AckBroadcast(
    messageId: Long,
    inReplyTo: Long,
  ) extends BroadcastRequestBody {
    override final val tpe: String = "broadcast_ok"
  }

  object AckBroadcast {
    given Decoder[AckBroadcast] =
      for {
        messageId <- Decoder[Long].at("msg_id")
        inReplyTo <- Decoder[Long].at("in_reply_to")
      } yield AckBroadcast(
        messageId = messageId,
        inReplyTo = inReplyTo
      )
  }

  case class Read(
    messageId: Long,
  ) extends BroadcastRequestBody {
    override final val tpe: String = "read"
  }

  object Read {
    given Decoder[Read] =
      for {
        messageId <- Decoder[Long].at("msg_id")
      } yield Read(
        messageId = messageId
      )
  }

  case class Topology(
    messageId: Long,
    topology: Map[String, List[String]],
  ) extends BroadcastRequestBody {
    override final val tpe: String = "topology"
  }

  object Topology {
    given Decoder[Topology] =
      for {
        messageId <- Decoder[Long].at("msg_id")
        topology  <- Decoder[Map[String, List[String]]].at("topology")
      } yield Topology(
        messageId = messageId,
        topology = topology,
      )
  }

}
