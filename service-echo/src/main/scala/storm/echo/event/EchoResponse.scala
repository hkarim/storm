package storm.echo.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type EchoResponse = Response[EchoResponseBody]

case class EchoResponseBody(
  messageId: Long,
  inReplyTo: Long,
  echo: String
) extends ResponseBody {
  override final val tpe: String = "echo_ok"
}

object EchoResponseBody {

  given Encoder[EchoResponseBody] = Encoder.instance[EchoResponseBody] { v =>
    Json.obj(
      "type"        -> v.tpe.asJson,
      "in_reply_to" -> v.inReplyTo.asJson,
      "echo"        -> v.echo.asJson,
    )
  }

}
