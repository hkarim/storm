package storm.echo.service

import cats.effect.*
import storm.context.ServiceContext
import storm.event.{Request, Response}
import storm.service.{InitializationService, NodeStream}

class EchoNodeStream(serviceContext: ServiceContext) extends NodeStream(serviceContext) {

  private final val initializationService: InitializationService =
    InitializationService.instance(serviceContext)
  private final val echoService: EchoService =
    EchoService.instance(serviceContext)

  def onRequest(request: Request): IO[Response] = request match {
    case request: Request.InitializationRequest =>
      initializationService.onRequest(request)
    case request: Request.EchoRequest =>
      echoService.onRequest(request)
  }

}

object EchoNodeStream {
  def instance(serviceContext: ServiceContext): EchoNodeStream =
    new EchoNodeStream(serviceContext)
}
