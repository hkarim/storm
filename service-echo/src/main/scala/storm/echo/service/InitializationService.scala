package storm.echo.service

import cats.effect.*
import storm.echo.context.ServiceContext
import storm.echo.node.NodeState
import storm.event.{Request, Response, ResponseBody}

class InitializationService(val serviceContext: ServiceContext) extends NodeService {

  def onRequest(request: Request.InitializationRequest): IO[Response] = 
    for {
      currentState <- serviceContext.nodeState.get
      r <- currentState match {
        case NodeState.Uninitialized =>
          val s = NodeState.Initialized(request.body.nodeId, request.body.nodeIds)
          for {
            _ <- serviceContext.nodeState.set(s)
            c <- serviceContext.messageCounter.getAndUpdate(_ + 1)
            response = Response.InitializationResponse(
              source = request.body.nodeId,
              destination = request.source,
              body = ResponseBody.InitializationResponse(
                messageId = Some(c),
                inReplyTo = request.body.messageId,
              )
            )
          } yield response
        case otherwise =>
         reportInvalidStateError(request, otherwise)
      }
    } yield r

}

object InitializationService {
  def instance(serviceContext: ServiceContext): InitializationService =
    new InitializationService(serviceContext)
}
