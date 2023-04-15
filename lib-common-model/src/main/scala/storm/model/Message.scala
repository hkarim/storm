package storm.model

import io.circe.*
import io.circe.syntax.*

trait DataType[-A] {
  def tpe(a: A): String
}

object DataType {
  def apply[A](using ev: DataType[A]): DataType[A] = ev

  def instance[A](f: A => String): DataType[A] = (a: A) => f(a)
}

case class Message[+A](
  messageId: Long,
  source: String,
  destination: String,
  data: A,
)

object Message {

  given [A: Encoder: DataType]: Encoder[Message[A]] =
    Encoder.instance[Message[A]] { v =>
      Json.obj(
        "src"  -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> Json.obj(
          "type"   -> DataType[A].tpe(v.data).asJson,
          "msg_id" -> v.messageId.asJson
        ).deepMerge(v.data.asJson)
      )
    }

  given [A: Decoder]: Decoder[Message[A]] =
    for {
      messageId <- Decoder[Long].prepare { c =>
        c.downField("body").downField("msg_id")
      }
      source      <- Decoder[String].at("src")
      destination <- Decoder[String].at("dest")
      data        <- Decoder[A].at("body")
    } yield Message[A](
      messageId = messageId,
      source = source,
      destination = destination,
      data = data,
    )

  object Encoders {
    def inReplyTo(value: Long): (String, Json) =
      "in_reply_to" -> value.asJson
  }

  object Decoders {
    def inReplyTo: Decoder[Long] =
      Decoder[Long].at("in_reply_to")
  }

  def response[Rq, Rs](request: Message[Rq], id: Long, data: Rs): Message[Rs] =
    Message[Rs](
      messageId = id,
      source = request.destination,
      destination = request.source,
      data = data,
    )

}
