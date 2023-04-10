package storm.echo.service

import cats.effect.*
import storm.context.*
import storm.event.*
import storm.service.NodeService

class EchoService(serviceContext: ServiceContext) extends NodeService(serviceContext) {

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

object EchoService {
  def instance(serviceContext: ServiceContext): EchoService =
    new EchoService(serviceContext)
}
