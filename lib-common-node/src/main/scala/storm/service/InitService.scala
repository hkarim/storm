package storm.service

import cats.effect.IO
import cats.effect.std.Queue
import io.circe.*
import io.circe.syntax.*
import storm.context.NodeState
import storm.model.*

class InitService(inbound: Queue[IO, String], outbound: Queue[IO, String]) {

  def run: IO[NodeState] =
    for {
      line        <- inbound.take
      jsonRequest <- IO.fromEither(parser.parse(line))
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
      _ <- outbound.offer(s"${response.asJson.noSpaces}\n")
    } yield state
}

object InitService {
  def instance(inbound: Queue[IO, String], outbound: Queue[IO, String]): InitService =
    new InitService(inbound, outbound)
}
