package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.model.*
import storm.service.NodeStream

class PullNodeStream(serviceContext: LocalServiceContext) extends NodeStream[BroadcastRequest, BroadcastResponse](serviceContext) {

  def onRequest(request: BroadcastRequest): IO[Option[BroadcastResponse]] =
    request.body match {
      case BroadcastRequestBody.Broadcast(messageId, message) =>
        for {
          id <- serviceContext.counter.getAndUpdate(_ + 1)
          _  <- serviceContext.messages.update(ms => (ms :+ message).sorted.distinct)
        } yield Some(
          Response(
            source = request.destination,
            destination = request.source,
            body = BroadcastResponseBody.Broadcast(
              messageId = id,
              inReplyTo = messageId,
            )
          )
        )

      case BroadcastRequestBody.Read(messageId) =>
        for {
          id <- serviceContext.counter.getAndUpdate(_ + 1)
          ms <- serviceContext.messages.get
        } yield Some(
          Response(
            source = request.destination,
            destination = request.source,
            body = BroadcastResponseBody.Read(
              messageId = id,
              inReplyTo = messageId,
              messages = ms,
            )
          )
        )

      case BroadcastRequestBody.AckRead(_, _, messages) =>
        serviceContext.messages.update(ms => (ms ++ messages).sorted.distinct)
          .map(_ => None)

      case BroadcastRequestBody.Topology(messageId, topology) =>
        for {
          id <- serviceContext.counter.getAndUpdate(_ + 1)
          _  <- serviceContext.topology.set(topology)
        } yield Some(
          Response(
            source = request.destination,
            destination = request.source,
            body = BroadcastResponseBody.Topology(
              messageId = id,
              inReplyTo = messageId,
            )
          )
        )

      case BroadcastRequestBody.AckBroadcast(_, _) =>
        IO.pure(None)
    }

}

object PullNodeStream {
  def instance(serviceContext: LocalServiceContext): PullNodeStream =
    new PullNodeStream(serviceContext)
}
