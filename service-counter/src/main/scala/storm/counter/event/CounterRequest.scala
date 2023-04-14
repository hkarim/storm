package storm.counter.event

import io.circe.*
import io.circe.syntax.*
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
        messageId <- Decoder[Long].at("msg_id")
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
        messageId <- Decoder[Long].at("msg_id")
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
        Json.obj(
          "type"   -> v.tpe.asJson,
          "msg_id" -> v.messageId.asJson,
        )
      }

    given Decoder[Pull] =
      for {
        messageId <- Decoder[Long].at("msg_id")
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
        messageId <- Decoder[Long].at("msg_id")
        inReplyTo <- Decoder[Long].at("in_reply_to")
        value     <- Decoder[Map[String, Int]].at("value")
      } yield AckPull(
        messageId = messageId,
        inReplyTo = inReplyTo,
        value = value,
      )

  }

}
