package storm.broadcast.service

import cats.effect.*
import storm.broadcast.event.*
import storm.broadcast.context.*
import storm.model.*
import storm.service.NodeStream

sealed trait InboundRequest {
  def widen: InboundRequest = this
}
object InboundRequest {
  import io.circe.*

  given Decoder[InboundRequest] = {
    Decoder.instance { c =>
      c.downField("body").downField("type").as[String].flatMap {
        case "broadcast_ok" =>
          Decoder.const(Ignore).map(_.widen).apply(c)
        case _ =>
          Decoder[BroadcastRequest].map(Handle.apply).map(_.widen).apply(c)
      }
    }
  }

  case class Handle(delegate: BroadcastRequest) extends InboundRequest

  case object Ignore extends InboundRequest
}

class BroadcastNodeStream(serviceContext: LocalServiceContext) extends NodeStream[InboundRequest, BroadcastResponse](serviceContext) {

  def onRequest(request: InboundRequest): IO[Option[BroadcastResponse]] =
    request match {
      case InboundRequest.Handle(delegate) =>
        onRequest(delegate)
      case InboundRequest.Ignore =>
        IO.pure(None)
    }

  def onRequest(request: BroadcastRequest): IO[Option[BroadcastResponse]] =
    serviceContext.messageCounter.getAndUpdate(_ + 1).flatMap { c =>
      request.body match {
        case BroadcastRequestBody.Broadcast(messageId, message) =>
          for {
            _ <- serviceContext.messages.update(ms => message :: ms)
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
      }

    }

}

object BroadcastNodeStream {
  def instance(serviceContext: LocalServiceContext): BroadcastNodeStream =
    new BroadcastNodeStream(serviceContext)
}
