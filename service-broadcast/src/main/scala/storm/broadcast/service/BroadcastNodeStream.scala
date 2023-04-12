package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.broadcast.model.BroadcastMessage
import storm.model.*
import storm.service.NodeStream

class BroadcastNodeStream(serviceContext: LocalServiceContext) extends NodeStream[BroadcastRequest, BroadcastResponse](serviceContext) {

  private final val id: Option[Long] = Some(-1L)

  def onRequest(request: BroadcastRequest): IO[Option[BroadcastResponse]] =
    request.body match {
      case BroadcastRequestBody.Broadcast(messageId, message) =>
        serviceContext.messages.get.flatMap { messages =>
          if messages.contains(message) then
            IO.pure(None) // we shouldn't send messages that we've seen before
          else
            for {
              _ <- serviceContext.messages.tryUpdate(ms => (ms :+ message).sorted)
              bm = BroadcastMessage(source = request.source, destination = request.destination, value = message)
              _ <- serviceContext.broadcastQueue.tryOffer(bm)
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
        }

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
