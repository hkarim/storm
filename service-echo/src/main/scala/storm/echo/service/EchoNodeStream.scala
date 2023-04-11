package storm.echo.service

import cats.effect.*
import storm.context.*
import storm.echo.event.*
import storm.model.*
import storm.service.NodeStream

class EchoNodeStream(serviceContext: ServiceContext) extends NodeStream[EchoRequest, EchoResponse](serviceContext) {

  def onRequest(request: EchoRequest): IO[EchoResponse] =
    serviceContext.messageCounter.getAndUpdate(_ + 1).map { c =>
      Response[EchoResponseBody](
        source = request.destination,
        destination = request.source,
        body = EchoResponseBody(
          messageId = Some(c),
          inReplyTo = request.body.messageId,
          echo = request.body.echo,
        )
      )
    }

}

object EchoNodeStream {
  def instance(serviceContext: ServiceContext): EchoNodeStream =
    new EchoNodeStream(serviceContext)
}
