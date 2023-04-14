package storm.echo.event

import io.circe.*
import storm.model.*

type EchoRequest = Request[EchoRequestBody]

case class EchoRequestBody(
  messageId: Long,
  echo: String
) extends RequestBody {
  override final val tpe: String = "echo"
}

object EchoRequestBody {
  given Decoder[EchoRequestBody] =
    for {
      messageId <- Decoders.messageId
      echo      <- Decoder[String].at("echo")
    } yield EchoRequestBody(
      messageId = messageId,
      echo = echo,
    )
}
