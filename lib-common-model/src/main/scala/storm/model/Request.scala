package storm.model

import io.circe.*
import io.circe.syntax.*

trait RequestBody {
  def tpe: String
  def messageId: Long
}

case class Request[+A <: RequestBody](
  source: String,
  destination: String,
  body: A,
)

object Request {

  given [A <: RequestBody: Encoder]: Encoder[Request[A]] =
    Encoder.instance[Request[A]] { v =>
      Json.obj(
        "src"  -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson,
      )
    }

  given [A <: RequestBody: Decoder]: Decoder[Request[A]] =
    for {
      source      <- Decoder[String].at("src")
      destination <- Decoder[String].at("dest")
      body        <- Decoder[A].at("body")
    } yield Request[A](
      source = source,
      destination = destination,
      body = body,
    )

}
