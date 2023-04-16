package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.model.*
import storm.service.NodeStream

class BroadcastNodeStream(serviceContext: BroadcastServiceContext)
  extends NodeStream[BroadcastRequestData, BroadcastResponseData](serviceContext) {

  def onRequest(request: Message[BroadcastRequestData]): IO[Option[BroadcastResponseData]] =
    request.data match {
      case BroadcastRequestData.Broadcast(message) =>
        serviceContext.messages.update(ms => (ms :+ message).sorted.distinct).map { _ =>
          Some(
            BroadcastSharedData.AckBroadcast(
              inReplyTo = request.messageId,
            )
          )
        }

      case BroadcastRequestData.Read =>
        serviceContext.messages.get.map { ms =>
          Some(
            BroadcastSharedData.AckRead(
              inReplyTo = request.messageId,
              messages = ms,
            )
          )
        }

      case BroadcastSharedData.AckRead(_, messages) =>
        serviceContext.messages.update(ms => (ms ++ messages).sorted.distinct)
          .map(_ => None)

      case BroadcastRequestData.Topology(topology) =>
        serviceContext.topology.set(topology).map { _ =>
          Some(
            BroadcastResponseData.AckTopology(
              inReplyTo = request.messageId,
            )
          )
        }
      case BroadcastSharedData.AckBroadcast(_) =>
        IO.pure(None)

    }

}

object BroadcastNodeStream {
  def instance(serviceContext: BroadcastServiceContext): BroadcastNodeStream =
    new BroadcastNodeStream(serviceContext)
}
