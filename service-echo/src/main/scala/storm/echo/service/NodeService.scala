package storm.echo.service

import cats.effect.IO
import storm.echo.context.ServiceContext
import storm.echo.node.NodeState
import storm.event.{Request, Response, ResponseBody}

trait NodeService {
  def serviceContext: ServiceContext

  def reportInvalidStateError(request: Request, currentState: NodeState): IO[Response] =
    for {
      c <- serviceContext.messageCounter.getAndUpdate(_ + 1)
      response = Response.ErrorResponse(
        source = request.destination,
        destination = request.source,
        body = ResponseBody.ErrorResponse(
          messageId = Some(c),
          inReplyTo = request.body.messageId,
          code = 1001,
          text = Some(s"unable to change node state to `Initialized`, current state: `${currentState.description}`"),
        )
      )
    } yield response
}
