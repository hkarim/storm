package storm.service

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import storm.context.NodeState
import storm.model.*

object InitService {

  def run: IO[NodeState] =
    for {
      line        <- IO.readLine
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
      _ <- IO.println(response.asJson.noSpaces)
    } yield state

}
