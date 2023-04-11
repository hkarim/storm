package storm.model

import io.circe.*
import io.circe.syntax.*

trait ResponseBody {
  def tpe: String
  def messageId: Option[Long]
  def inReplyTo: Option[Long]
}

case class Response[+A <: ResponseBody](
  source: String,
  destination: String,
  body: A,
)

object Response {

  given[A <: ResponseBody : Encoder]: Encoder[Response[A]] =
    Encoder.instance[Response[A]] { v =>
      Json.obj(
        "src" -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson,
      )
    }

}
