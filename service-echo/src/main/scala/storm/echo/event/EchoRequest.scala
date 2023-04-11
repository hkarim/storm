package storm.echo.event

import io.circe.*
import storm.model.*

type EchoRequest = Request[EchoRequestBody]

case class EchoRequestBody(
  messageId: Option[Long],
  echo: String
) extends RequestBody {
  override final val tpe: String = "echo"
}

object EchoRequestBody {
  given Decoder[EchoRequestBody] =
    for {
      messageId <- Decoder[Option[Long]].at("msg_id")
      echo      <- Decoder[String].at("echo")
    } yield EchoRequestBody(
      messageId = messageId,
      echo = echo,
    )
}
