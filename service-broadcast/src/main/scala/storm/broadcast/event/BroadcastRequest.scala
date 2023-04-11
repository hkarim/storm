package storm.broadcast.event

import io.circe.{Decoder, DecodingFailure}
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
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized broadcast request type `$otherwise`", Nil))
    }

  case class Broadcast(
    messageId: Option[Long],
    message: Long,
  ) extends BroadcastRequestBody {
    override final val tpe: String = "broadcast"
  }

  object Broadcast {
    given Decoder[Broadcast] =
      for {
        messageId <- Decoder[Option[Long]].at("msg_id")
        message   <- Decoder[Long].at("message")
      } yield Broadcast(
        messageId = messageId,
        message = message,
      )
  }

  case class Read(
    messageId: Option[Long],
  ) extends BroadcastRequestBody {
    override final val tpe: String = "read"
  }

  object Read {
    given Decoder[Read] =
      for {
        messageId <- Decoder[Option[Long]].at("msg_id")
      } yield Read(
        messageId = messageId
      )
  }

  case class Topology(
    messageId: Option[Long],
    topology: Map[String, List[String]],
  ) extends BroadcastRequestBody {
    override final val tpe: String = "topology"
  }

  object Topology {
    given Decoder[Topology] =
      for {
        messageId <- Decoder[Option[Long]].at("msg_id")
        topology  <- Decoder[Map[String, List[String]]].at("topology")
      } yield Topology(
        messageId = messageId,
        topology = topology,
      )
  }

}
