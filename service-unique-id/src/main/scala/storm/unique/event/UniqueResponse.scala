package storm.unique.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

type UniqueResponse = Response[UniqueResponseBody]

case class UniqueResponseBody(
  messageId: Long,
  inReplyTo: Long,
  id: String
) extends ResponseBody {
  override final val tpe: String = "generate_ok"
}

object UniqueResponseBody {

  given Encoder[UniqueResponseBody] = Encoder.instance[UniqueResponseBody] { v =>
    Encoders.response(v, "id" -> v.id.asJson)
  }

}
