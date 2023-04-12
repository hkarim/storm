package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.model.*
import storm.service.NodeStream

class BroadcastNodeStream(serviceContext: LocalServiceContext) extends NodeStream[BroadcastRequest, BroadcastResponse](serviceContext) {

  def onRequest(request: BroadcastRequest): IO[Option[BroadcastResponse]] =
    serviceContext.messageCounter.getAndUpdate(_ + 1).flatMap { c =>
      request.body match {
        case BroadcastRequestBody.Broadcast(messageId, message) =>
          for {
            _ <- serviceContext.messages.update(ms => (message :: ms).distinct)
            _ <- serviceContext.queue.offer(message)
          } yield Some(
            Response(
              source = request.destination,
              destination = request.source,
              body = BroadcastResponseBody.Broadcast(
                messageId = Some(c),
                inReplyTo = messageId,
              )
            )
          )
        case BroadcastRequestBody.Read(messageId) =>
          for {
            ms <- serviceContext.messages.get
          } yield Some(
            Response(
              source = request.destination,
              destination = request.source,
              body = BroadcastResponseBody.Read(
                messageId = Some(c),
                inReplyTo = messageId,
                messages = ms,
              )
            )
          )
        case BroadcastRequestBody.Topology(messageId, topology) =>
          for {
            _ <- serviceContext.topology.set(topology)
          } yield Some(
            Response(
              source = request.destination,
              destination = request.source,
              body = BroadcastResponseBody.Topology(
                messageId = Some(c),
                inReplyTo = messageId,
              )
            )
          )

        case _: BroadcastRequestBody.AckBroadcast =>
          IO.pure(None)
      }

    }

}

object BroadcastNodeStream {
  def instance(serviceContext: LocalServiceContext): BroadcastNodeStream =
    new BroadcastNodeStream(serviceContext)
}
