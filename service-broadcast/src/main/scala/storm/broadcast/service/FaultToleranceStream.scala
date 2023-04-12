package storm.broadcast.service

import cats.effect.*
import fs2.*

import scala.concurrent.duration.*
import storm.broadcast.context.LocalServiceContext

class FaultToleranceStream(serviceContext: LocalServiceContext) {
  def run: IO[Unit] =
    Stream
      .awakeEvery[IO](1.second)
      .evalMap(_ => serviceContext.inFlight.get)
      .filter(_.nonEmpty)
      .flatMap { inFlight =>
        Stream
          .iterable(inFlight)
          .evalMap {
            case (_, bm) => serviceContext.broadcastQueue.offer(bm)
          }
      }
      .compile
      .drain
}

object FaultToleranceStream {
  def instance(serviceContext: LocalServiceContext): FaultToleranceStream =
    new FaultToleranceStream(serviceContext)
}
