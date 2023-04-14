package storm.counter.service

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import storm.counter.context.CounterServiceContext

import scala.concurrent.duration.*
import storm.counter.event.CounterRequestBody
import storm.model.Request

class PullStream(serviceContext: CounterServiceContext) {

  def run: IO[Unit] = {
    val state      = serviceContext.state
    val neighbors = state.nodeIds.filterNot(_ == state.nodeId).toVector
    fs2.Stream
      .awakeEvery[IO](50.milliseconds)
      .evalMap { _ =>
        neighbors
          .traverse { neighbor =>
            serviceContext.counter.getAndUpdate(_ + 1).flatMap { c =>
              val request = Request(
                source = state.nodeId,
                destination = neighbor,
                body = CounterRequestBody.Pull(
                  messageId = c,
                )
              )
              serviceContext.outbound.tryOffer(request.asJson)
            }
          }
      }
      .compile
      .drain
  }
}

object PullStream {
  def instance(serviceContext: CounterServiceContext): PullStream =
    new PullStream(serviceContext)
}
