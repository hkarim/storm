package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.model.*
import storm.service.NodeStream

class BroadcastNodeStream(serviceContext: LocalServiceContext) extends NodeStream[BroadcastRequest, BroadcastResponse](serviceContext) {

  private final val id: Option[Long] = Some(-1L)

  def onRequest(request: BroadcastRequest): IO[Option[BroadcastResponse]] =
    request.body match {
      case BroadcastRequestBody.Broadcast(messageId, message) =>
        for {
          _ <- serviceContext.messages.tryUpdate(ms => (ms :+ message).sorted.distinct)
          _ <- serviceContext.messageQueue.tryOffer(message)
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
        serviceContext.messages.get.map { ms =>
          Some(
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
        }
      case BroadcastRequestBody.Topology(messageId, topology) =>
        for {
          _ <- serviceContext.topology.set(topology)
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

      case _: BroadcastRequestBody.AckBroadcast =>
        IO.pure(None)
    }

}

object BroadcastNodeStream {
  def instance(serviceContext: LocalServiceContext): BroadcastNodeStream =
    new BroadcastNodeStream(serviceContext)
}
