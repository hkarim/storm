package storm.unique.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*

case object UniqueRequestData {
  final val Type: String = "generate"

  given DataType[UniqueRequestData.type] =
    DataType.instance(_ => UniqueRequestData.Type)

  given Decoder[UniqueRequestData.type] =
    Decoder.const(UniqueRequestData)
}

case class UniqueResponseData(inReplyTo: Long, id: String)

object UniqueResponseData {
  final val Type: String = "generate_ok"

  given DataType[UniqueResponseData] =
    DataType.instance(_ => UniqueResponseData.Type)

  given Encoder[UniqueResponseData] =
    Encoder.instance[UniqueResponseData] { v =>
      Json.obj(
        Message.Encoders.inReplyTo(v.inReplyTo),
        "id" -> v.id.asJson
      )
    }

}
