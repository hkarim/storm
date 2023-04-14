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

}
