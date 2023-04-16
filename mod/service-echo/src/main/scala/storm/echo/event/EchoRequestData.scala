package storm.echo.event

import io.circe.*
import storm.model.*

case class EchoRequestData(echo: String)

object EchoRequestData {
  final val Type: String = "echo"

  given DataType[EchoRequestData] =
    DataType.instance(_ => Type)

  given Decoder[EchoRequestData] =
    for {
      echo <- Decoder[String].at("echo")
    } yield EchoRequestData(
      echo = echo,
    )
}
