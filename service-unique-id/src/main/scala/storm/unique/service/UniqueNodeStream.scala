package storm.unique.service

import cats.effect.*
import storm.context.*
import storm.unique.event.*
import storm.model.*
import storm.service.NodeStream

class UniqueNodeStream(serviceContext: ServiceContext) extends NodeStream[UniqueRequest, UniqueResponse](serviceContext) {

  def onRequest(request: UniqueRequest): IO[Option[UniqueResponse]] =
    serviceContext.counter.getAndUpdate(_ + 1).map { c =>
      Some(
        Response[UniqueResponseBody](
          source = request.destination,
          destination = request.source,
          body = UniqueResponseBody(
            messageId = c,
            inReplyTo = request.body.messageId,
            id = s"${serviceContext.state.nodeId}-$c",
          )
        )
      )
    }

}

object UniqueNodeStream {
  def instance(serviceContext: ServiceContext): UniqueNodeStream =
    new UniqueNodeStream(serviceContext)
}
