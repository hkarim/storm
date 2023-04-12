package storm.broadcast.service

import cats.effect.*
import cats.syntax.all.*
import io.circe.*
import storm.broadcast.context.LocalServiceContext
import storm.broadcast.event.BroadcastRequestBody
import storm.context.NodeState
import storm.model.Request

class PublishStream(serviceContext: LocalServiceContext) {

  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(serviceContext.messageQueue, 2048)
      .evalMap(broadcast)
      .flatMap { requests =>
        fs2.Stream
          .emits(requests)
          .map(json)
          // .through(fs2.text.utf8.encode[IO])
          // .through(fs2.io.stdout[IO])
          .evalMap(serviceContext.stdoutQueue.offer)
      }
      .compile
      .drain

  private def broadcast(value: Int): IO[List[Request[BroadcastRequestBody.Broadcast]]] =
    for {
      nodeState <- serviceContext.nodeState.get
      topology  <- serviceContext.topology.get
      request <- nodeState match {
        case NodeState.Uninitialized =>
          IO.pure(Nil)
        case NodeState.Initialized(nodeId, _) =>
          topology.get(nodeId) match {
            case Some(neighbors) =>
              neighbors.parTraverse { neighbor =>
                for {
                  c <- serviceContext.messageCounter.getAndUpdate(_ + 1)
                } yield Request(
                  source = nodeId,
                  destination = neighbor,
                  body = BroadcastRequestBody.Broadcast(
                    messageId = Some(c),
                    message = value,
                  )
                )
              }
            case None =>
              IO.pure(Nil)
          }

      }
    } yield request

  private def json[A: Encoder](model: A): String = {
    import io.circe.syntax.*
    s"${model.asJson.noSpaces}\n"
  }
}

object PublishStream {
  def instance(serviceContext: LocalServiceContext): PublishStream =
    new PublishStream(serviceContext)
}
