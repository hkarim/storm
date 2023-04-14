package storm.broadcast.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type BroadcastResponse = Response[BroadcastResponseBody]

sealed trait BroadcastResponseBody extends ResponseBody

object BroadcastResponseBody {

  given Encoder[BroadcastResponseBody] =
    Encoder.instance[BroadcastResponseBody] {
      case v: Broadcast =>
        Encoder[Broadcast].apply(v)
      case v: Read =>
        Encoder[Read].apply(v)
      case v: Topology =>
        Encoder[Topology].apply(v)
    }

  case class Broadcast(
    messageId: Long,
    inReplyTo: Long,
  ) extends BroadcastResponseBody {
    override final val tpe: String = "broadcast_ok"
  }

  object Broadcast {
    given Encoder[Broadcast] =
      Encoder.instance[Broadcast] { v =>
        Encoders.response(v)
      }
  }

  case class Read(
    messageId: Long,
    inReplyTo: Long,
    messages: Vector[Int],
  ) extends BroadcastResponseBody {
    override final val tpe: String = "read_ok"
  }

  object Read {
    given Encoder[Read] =
      Encoder.instance[Read] { v =>
        Encoders.response(v, "messages" -> v.messages.asJson)
      }
  }

  case class Topology(
    messageId: Long,
    inReplyTo: Long,
  ) extends BroadcastResponseBody {
    override final val tpe: String = "topology_ok"
  }

  object Topology {
    given Encoder[Topology] =
      Encoder.instance[Topology] { v =>
        Encoders.response(v)
      }
  }

}
