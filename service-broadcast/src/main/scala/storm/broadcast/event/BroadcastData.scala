package storm.broadcast.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

sealed trait BroadcastRequestData {
  def widen: BroadcastRequestData = this
}

object BroadcastRequestData {

  given DataType[BroadcastRequestData] =
    DataType.instance[BroadcastRequestData] {
      case _: Broadcast =>
        Broadcast.Type
      case _: BroadcastSharedData.AckBroadcast =>
        BroadcastSharedData.AckBroadcast.Type
      case Read =>
        Read.Type
      case _: BroadcastSharedData.AckRead =>
        BroadcastSharedData.AckRead.Type
      case _: Topology =>
        Topology.Type
    }

  given Decoder[BroadcastRequestData] =
    Decoder[String].at("type").flatMap {
      case Broadcast.Type =>
        Decoder[Broadcast].map(_.widen)
      case BroadcastSharedData.AckBroadcast.Type =>
        Decoder[BroadcastSharedData.AckBroadcast].map(_.widen)
      case Read.Type =>
        Decoder[Read.type].map(_.widen)
      case BroadcastSharedData.AckRead.Type =>
        Decoder[BroadcastSharedData.AckRead].map(_.widen)
      case Topology.Type =>
        Decoder[Topology].map(_.widen)

      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized broadcast request type `$otherwise`", Nil))
    }

  case class Broadcast(message: Int) extends BroadcastRequestData

  object Broadcast {
    final val Type: String = "broadcast"
    given Encoder[Broadcast] =
      Encoder.instance[Broadcast] { v =>
        Json.obj("message" -> v.message.asJson)
      }

    given Decoder[Broadcast] =
      for {
        message <- Decoder[Int].at("message")
      } yield Broadcast(
        message = message,
      )
  }

  case object Read extends BroadcastRequestData {
    final val Type: String = "read"
    given Decoder[Read.type] =
      Decoder.const(Read)
    given Encoder[Read.type] =
      Encoder.instance[Read.type] { _ =>
        Json.obj()
      }
  }

  case class Topology(topology: Map[String, List[String]]) extends BroadcastRequestData

  object Topology {
    final val Type: String = "topology"
    given Decoder[Topology] =
      for {
        topology <- Decoder[Map[String, List[String]]].at("topology")
      } yield Topology(
        topology = topology,
      )
  }

}

sealed trait BroadcastResponseData

object BroadcastResponseData {

  given DataType[BroadcastResponseData] =
    DataType.instance[BroadcastResponseData] {
      case _: AckTopology =>
        AckTopology.Type
      case _: BroadcastSharedData.AckRead =>
        BroadcastSharedData.AckRead.Type
      case _: BroadcastSharedData.AckBroadcast =>
        BroadcastSharedData.AckBroadcast.Type
    }

  given Encoder[BroadcastResponseData] =
    Encoder.instance[BroadcastResponseData] {
      case v: AckTopology =>
        Encoder[AckTopology].apply(v)
      case v: BroadcastSharedData.AckBroadcast =>
        Encoder[BroadcastSharedData.AckBroadcast].apply(v)
      case v: BroadcastSharedData.AckRead =>
        Encoder[BroadcastSharedData.AckRead].apply(v)

    }

  case class AckTopology(inReplyTo: Long) extends BroadcastResponseData

  object AckTopology {
    final val Type: String = "topology_ok"
    given Encoder[AckTopology] =
      Encoder.instance[AckTopology] { v =>
        Json.obj(Message.Encoders.inReplyTo(v.inReplyTo))
      }
  }

}

sealed trait BroadcastSharedData
  extends BroadcastRequestData with BroadcastResponseData

object BroadcastSharedData {

  case class AckRead(inReplyTo: Long, messages: Vector[Int]) extends BroadcastSharedData

  object AckRead {
    final val Type: String = "read_ok"

    given Encoder[AckRead] =
      Encoder.instance[AckRead] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "messages" -> v.messages.asJson,
        )
      }

    given Decoder[AckRead] = {
      for {
        inReplyTo <- Message.Decoders.inReplyTo
        messages  <- Decoder[Vector[Int]].at("messages")
      } yield AckRead(
        inReplyTo = inReplyTo,
        messages = messages,
      )
    }
  }

  // in multi-node broadcast workload,
  // we receive the same message as the
  // broadcast response
  case class AckBroadcast(inReplyTo: Long) extends BroadcastSharedData

  object AckBroadcast {
    final val Type: String = "broadcast_ok"

    given Encoder[AckBroadcast] =
      Encoder.instance[AckBroadcast] { v =>
        Json.obj(Message.Encoders.inReplyTo(v.inReplyTo))
      }

    given Decoder[AckBroadcast] =
      for {
        inReplyTo <- Message.Decoders.inReplyTo
      } yield AckBroadcast(
        inReplyTo = inReplyTo
      )
  }

}
