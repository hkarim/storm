package storm.model

import io.circe.*
import io.circe.syntax.*

object Encoders {
  def tpe[A <: MessageBody](v: A): (String, Json) = "type" -> v.tpe.asJson

  def messageId[A <: MessageBody](v: A): (String, Json) = "msg_id" -> v.messageId.asJson

  def inReplyTo[A <: ResponseBody](v: A): (String, Json) = "in_reply_to" -> v.inReplyTo.asJson

  def request[A <: RequestBody](v: A, fields: (String, Json)*): Json =
    Json.obj(
      (Vector(tpe(v), messageId(v)) ++ Vector(fields*))*
    )

  def response[A <: ResponseBody](v: A, fields: (String, Json)*): Json =
    Json.obj(
      (Vector(tpe(v), messageId(v), inReplyTo(v)) ++ Vector(fields*))*
    )
}
