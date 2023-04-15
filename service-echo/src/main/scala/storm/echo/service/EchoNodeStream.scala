package storm.echo.service

import cats.effect.*
import storm.context.*
import storm.echo.event.*
import storm.model.*
import storm.service.NodeStream

class EchoNodeStream(serviceContext: ServiceContext) extends NodeStream[EchoRequestData, EchoResponseData](serviceContext) {

  override def onRequest(request: Message[EchoRequestData]): IO[Option[EchoResponseData]] =
    IO.pure {
      Some(
        EchoResponseData(
          inReplyTo = request.messageId,
          echo = request.data.echo,
        )
      )
    }

}

object EchoNodeStream {
  def instance(serviceContext: ServiceContext): EchoNodeStream =
    new EchoNodeStream(serviceContext)
}
