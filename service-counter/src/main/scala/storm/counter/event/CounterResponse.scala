package storm.counter.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type CounterResponse = Response[CounterResponseBody]

sealed trait CounterResponseBody extends ResponseBody

object CounterResponseBody {

  given Encoder[CounterResponseBody] =
    Encoder.instance[CounterResponseBody] {
      case v: Add =>
        Encoder[Add].apply(v)
      case v: Read =>
        Encoder[Read].apply(v)
    }

  case class Add(
    messageId: Long,
    inReplyTo: Long,
  ) extends CounterResponseBody {
    override final val tpe: String = "add_ok"
  }

  object Add {
    given Encoder[Add] =
      Encoder.instance[Add] { v =>
        Json.obj(
          "type"        -> v.tpe.asJson,
          "msg_id"      -> v.messageId.asJson,
          "in_reply_to" -> v.inReplyTo.asJson,
        )
      }
  }

  case class Read(
    messageId: Long,
    inReplyTo: Long,
    value: Int,
  ) extends CounterResponseBody {
    override final val tpe: String = "read_ok"
  }

  object Read {
    given Encoder[Read] =
      Encoder.instance[Read] { v =>
        Json.obj(
          "type"        -> v.tpe.asJson,
          "msg_id"      -> v.messageId.asJson,
          "in_reply_to" -> v.inReplyTo.asJson,
          "value"       -> v.value.asJson,
        )
      }
  }

}
