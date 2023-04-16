package storm.unique.service

import cats.effect.*
import storm.model.*
import storm.service.NodeStream
import storm.unique.context.UniqueServiceContext
import storm.unique.event.*

class UniqueNodeStream(serviceContext: UniqueServiceContext)
  extends NodeStream[UniqueRequestData.type, UniqueResponseData](serviceContext) {

  def onRequest(request: Message[UniqueRequestData.type]): IO[Option[UniqueResponseData]] =
    serviceContext.unique.getAndUpdate(_ + 1).map { u =>
      Some(
        UniqueResponseData(
          inReplyTo = request.messageId,
          id = s"${serviceContext.state.nodeId}-$u",
        )
      )
    }

}

object UniqueNodeStream {
  def instance(serviceContext: UniqueServiceContext): UniqueNodeStream =
    new UniqueNodeStream(serviceContext)
}
