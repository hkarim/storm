package storm.model

import io.circe.Decoder

trait RequestBody {
  def tpe: String
  def messageId: Option[Long]
}

case class Request[+A <: RequestBody](
  source: String,
  destination: String,
  body: A,
)

object Request {

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
