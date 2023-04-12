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
      request     <- IO.fromEither(jsonRequest.as[Request[InitializationRequestBody]])
      state = NodeState(request.body.nodeId, request.body.nodeIds)
      response = Response(
        source = request.body.nodeId,
        destination = request.source,
        body = InitializationResponseBody(
          messageId = Some(-1L),
          inReplyTo = request.body.messageId,
        )
      )
      _ <- outbound.offer(response.asJson)
    } yield state
}

object InitService {
  def instance(inbound: Queue[IO, Json], outbound: Queue[IO, Json]): InitService =
    new InitService(inbound, outbound)
}
