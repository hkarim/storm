package storm.service

import cats.effect.IO
import cats.effect.std.Queue
import io.circe.*
import io.circe.syntax.*
import storm.context.NodeState
import storm.model.*

class InitService(inbound: Queue[IO, Json], outbound: Queue[IO, Json]) {

  def run: IO[NodeState] =
    for {
      jsonRequest <- inbound.take
      request     <- IO.fromEither(jsonRequest.as[Message[InitializationRequestData]])
      state = NodeState(request.data.nodeId, request.data.nodeIds)
      response = Message(
        messageId = -1L,
        source = request.data.nodeId,
        destination = request.source,
        data = InitializationResponseData(
          inReplyTo = request.messageId,
        )
      )
      _ <- outbound.offer(response.asJson)
    } yield state
}

object InitService {
  def instance(inbound: Queue[IO, Json], outbound: Queue[IO, Json]): InitService =
    new InitService(inbound, outbound)
}
