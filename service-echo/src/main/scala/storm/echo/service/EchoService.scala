package storm.echo.service

import cats.effect.*
import storm.echo.context.ServiceContext
import storm.echo.node.NodeState
import storm.event.*

class EchoService(val serviceContext: ServiceContext) extends NodeService {
  def onRequest(request: Request.EchoRequest): IO[Response] =
    for {
      currentState <- serviceContext.nodeState.get
      r <- currentState match {
        case NodeState.Initialized(nodeId, _) =>
          for {
            c <- serviceContext.messageCounter.getAndUpdate(_ + 1)
            response = Response.EchoResponse(
              source = nodeId,
              destination = request.source,
              body = ResponseBody.EchoResponse(
                messageId = Some(c),
                inReplyTo = request.body.messageId,
                echo = request.body.echo,
              )
            )
          } yield response
        case otherwise =>
          reportInvalidStateError(request, otherwise)
      }
    } yield r
}
