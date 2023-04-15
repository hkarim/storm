package storm.broadcast.service

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import storm.broadcast.context.BroadcastServiceContext

import scala.concurrent.duration.*
import storm.broadcast.event.BroadcastRequestData
import storm.model.*

class ReadStream(serviceContext: BroadcastServiceContext) {

  def run: IO[Unit] = {
    val state      = serviceContext.state
    val candidates = state.nodeIds.filterNot(_ == state.nodeId).toVector
    val groups     = candidates.grouped(7).toVector
    val length     = groups.length
    fs2.Stream
      .awakeEvery[IO](150.milliseconds)
      .map(_ => scala.util.Random.between(0, 10000))
      .evalMap { random =>
        val neighbors = groups(random % length)
        neighbors
          .traverse { neighbor =>
            serviceContext.counter.getAndUpdate(_ + 1).flatMap { c =>
              val request = Message(
                source = serviceContext.state.nodeId,
                destination = neighbor,
                messageId = c,
                data = BroadcastRequestData.Read
              )
              serviceContext.outbound.tryOffer(request.asJson)
            }
          }
      }
      .compile
      .drain
  }
}

object ReadStream {
  def instance(serviceContext: BroadcastServiceContext): ReadStream =
    new ReadStream(serviceContext)
}
