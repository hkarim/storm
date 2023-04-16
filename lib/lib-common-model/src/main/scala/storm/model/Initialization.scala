package storm.model

import io.circe.*

case class InitializationRequestData(nodeId: String, nodeIds: List[String])

object InitializationRequestData {
  final val Type: String = "init"

  given DataType[InitializationRequestData] =
    DataType.instance(_ => Type)

  given Decoder[InitializationRequestData] =
    for {
      nodeId  <- Decoder[String].at("node_id")
      nodeIds <- Decoder[List[String]].at("node_ids")
    } yield InitializationRequestData(
      nodeId = nodeId,
      nodeIds = nodeIds,
    )
}

case class InitializationResponseData(inReplyTo: Long)

object InitializationResponseData {
  final val Type: String = "init_ok"

  given DataType[InitializationResponseData] =
    DataType.instance(_ => Type)

  given Encoder[InitializationResponseData] =
    Encoder.instance[InitializationResponseData] { v =>
      Json.obj(
        Message.Encoders.inReplyTo(v.inReplyTo)
      )
    }
}
