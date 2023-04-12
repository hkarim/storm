package storm.broadcast.service

import cats.effect.*
import io.circe.*
import storm.broadcast.context.LocalServiceContext
import storm.broadcast.event.BroadcastRequestBody
import storm.broadcast.model.BroadcastMessage
import storm.model.Request

class PublishStream(serviceContext: LocalServiceContext) {

  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(serviceContext.messageQueue, 2048)
      .evalMap(broadcast)
      .filter(_.nonEmpty)
      .flatMap { requests =>
        fs2.Stream
          .emits(requests)
          .map(json)
          .evalMap(serviceContext.outbound.offer)
      }
      .compile
      .drain

  private def broadcast(message: BroadcastMessage): IO[List[Request[BroadcastRequestBody.Broadcast]]] =
    for {
      topology <- serviceContext.topology.get
      c        <- serviceContext.messageCounter.getAndUpdate(_ + 1)
      nodeState = serviceContext.nodeState
      requests = topology.get(nodeState.nodeId) match {
        case Some(neighbors) =>
          neighbors
            .filterNot(_ == message.source) // don't send the message back to its sender
            .map { neighbor =>
              Request(
                source = nodeState.nodeId,
                destination = neighbor,
                body = BroadcastRequestBody.Broadcast(
                  messageId = Some(c),
                  message = message.value,
                )
              )
            }
        case None =>
          Nil
      }
    } yield requests

  private def json[A: Encoder](model: A): String = {
    import io.circe.syntax.*
    s"${model.asJson.noSpaces}\n"
  }
}

object PublishStream {
  def instance(serviceContext: LocalServiceContext): PublishStream =
    new PublishStream(serviceContext)
}
