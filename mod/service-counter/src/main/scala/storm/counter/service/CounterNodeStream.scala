package storm.counter.service

import cats.effect.*
import storm.model.*
import storm.counter.context.CounterServiceContext
import storm.counter.event.*
import storm.service.NodeStream

class CounterNodeStream(serviceContext: CounterServiceContext) extends NodeStream[CounterRequestData, CounterResponseData](serviceContext) {

  def onRequest(request: Message[CounterRequestData]): IO[Option[CounterResponseData]] =
    request.data match {
      case CounterRequestData.Add(delta) =>
        val key = s"${request.source}-${request.messageId}"
        for {
          _ <- serviceContext.delta.getAndUpdate(_.updated(key, delta))
        } yield Some(
          CounterResponseData.Add(
            inReplyTo = request.messageId,
          )
        )

      case CounterRequestData.Read =>
        for {
          delta <- serviceContext.delta.get
        } yield Some(
          CounterResponseData.Read(
            inReplyTo = request.messageId,
            value = delta.values.sum
          )
        )

      case CounterRequestData.Pull =>
        for {
          delta <- serviceContext.delta.get
        } yield Some(
          CounterSharedData.AckPull(
            inReplyTo = request.messageId,
            value = delta,
          )
        )

      case CounterSharedData.AckPull(_, value) =>
        serviceContext.delta.getAndUpdate(_ ++ value).map(_ => None)

    }

}

object CounterNodeStream {
  def instance(serviceContext: CounterServiceContext): CounterNodeStream =
    new CounterNodeStream(serviceContext)
}
