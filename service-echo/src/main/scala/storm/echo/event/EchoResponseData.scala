package storm.echo.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

case class EchoResponseData(
  inReplyTo: Long,
  echo: String
)

object EchoResponseData {
  final val Type: String = "echo_ok"

  given DataType[EchoResponseData] =
    DataType.instance(_ => Type)

  given Encoder[EchoResponseData] = Encoder.instance[EchoResponseData] { v =>
    Json.obj(
      Message.Encoders.inReplyTo(v.inReplyTo),
      "echo" -> v.echo.asJson
    )
  }

}
