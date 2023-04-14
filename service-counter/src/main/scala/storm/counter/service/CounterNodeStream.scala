package storm.counter.service

import cats.effect.*
import storm.model.*
import storm.counter.context.CounterServiceContext
import storm.counter.event.*
import storm.service.NodeStream

class CounterNodeStream(serviceContext: CounterServiceContext) extends NodeStream[CounterRequest, CounterResponse](serviceContext) {

  def onRequest(request: CounterRequest): IO[Option[CounterResponse]] =
    request.body match {
      case CounterRequestBody.Add(messageId, delta) =>
        for {
          c <- serviceContext.counter.getAndUpdate(_ + 1)
          key = s"${request.source}-$messageId"
          _ <- serviceContext.delta.getAndUpdate(_.updated(key, delta))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = CounterResponseBody.Add(
              messageId = c,
              inReplyTo = request.body.messageId,
            )
          )
        )
      case CounterRequestBody.Read(messageId) =>
        for {
          c     <- serviceContext.counter.getAndUpdate(_ + 1)
          delta <- serviceContext.delta.get
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = CounterResponseBody.Read(
              messageId = c,
              inReplyTo = messageId,
              value = delta.values.sum
            )
          )
        )
    }

}

object CounterNodeStream {
  def instance(serviceContext: CounterServiceContext): CounterNodeStream =
    new CounterNodeStream(serviceContext)
}
