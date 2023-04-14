package storm.counter.event

import io.circe.*
import storm.model.*

type CounterRequest = Request[CounterRequestBody]

sealed trait CounterRequestBody extends RequestBody {
  def widen: CounterRequestBody = this
}

object CounterRequestBody {

  given Decoder[CounterRequestBody] =
    Decoder[String].at("type").flatMap {
      case "add" =>
        Decoder[Add].map(_.widen)
      case "read" =>
        Decoder[Read].map(_.widen)
      case "pull" =>
        Decoder[Pull].map(_.widen)
      case "pull_ok" =>
        Decoder[AckPull].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized counter request type `$otherwise`", Nil))
    }

  case class Add(
    messageId: Long,
    delta: Int,
  ) extends CounterRequestBody {
    override final val tpe: String = "add"
  }

  object Add {
    given Decoder[Add] =
      for {
        messageId <- Decoders.messageId
        delta     <- Decoder[Int].at("delta")
      } yield Add(
        messageId = messageId,
        delta = delta,
      )

  }

  case class Read(
    messageId: Long,
  ) extends CounterRequestBody {
    override final val tpe: String = "read"
  }

  object Read {
    given Decoder[Read] =
      for {
        messageId <- Decoders.messageId
      } yield Read(
        messageId = messageId,
      )

  }

  case class Pull(
    messageId: Long,
  ) extends CounterRequestBody {
    override final val tpe: String = "pull"
  }

  object Pull {
    given Encoder[Pull] =
      Encoder.instance[Pull] { v =>
        Encoders.request(v)
      }

    given Decoder[Pull] =
      for {
        messageId <- Decoders.messageId
      } yield Pull(
        messageId = messageId,
      )

  }

  case class AckPull(
    messageId: Long,
    inReplyTo: Long,
    value: Map[String, Int],
  ) extends CounterRequestBody {
    override final val tpe: String = "pull_ok"
  }

  object AckPull {
    given Decoder[AckPull] =
      for {
        messageId <- Decoders.messageId
        inReplyTo <- Decoders.inReplyTo
        value     <- Decoder[Map[String, Int]].at("value")
      } yield AckPull(
        messageId = messageId,
        inReplyTo = inReplyTo,
        value = value,
      )

  }

}
