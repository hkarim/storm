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
      case "read_ok" =>
        Decoder[AckRead].map(_.widen)
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
        Encoders.request(v, "message" -> v.message.asJson)
      }

    given Decoder[Broadcast] =
      for {
        messageId <- Decoders.messageId
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
        messageId <- Decoders.messageId
        inReplyTo <- Decoders.inReplyTo
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
        messageId <- Decoders.messageId
      } yield Read(
        messageId = messageId
      )

    given Encoder[Read] =
      Encoder.instance[Read] { v =>
        Encoders.request(v)
      }
  }

  case class AckRead(
    messageId: Long,
    inReplyTo: Long,
    messages: Vector[Int],
  ) extends BroadcastRequestBody {
    override final val tpe: String = "read_ok"
  }

  object AckRead {
    given Decoder[AckRead] = {
      for {
        messageId <- Decoders.messageId
        inReplyTo <- Decoders.inReplyTo
        messages  <- Decoder[Vector[Int]].at("messages")
      } yield AckRead(
        messageId = messageId,
        inReplyTo = inReplyTo,
        messages = messages,
      )
    }
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
        messageId <- Decoders.messageId
        topology  <- Decoder[Map[String, List[String]]].at("topology")
      } yield Topology(
        messageId = messageId,
        topology = topology,
      )
  }

}
