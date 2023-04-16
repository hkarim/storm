package storm.txn.service

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import storm.txn.context.TxnServiceContext
import storm.txn.event.TxnRequestData
import storm.model.*

import scala.concurrent.duration.*

class PullStream(serviceContext: TxnServiceContext) {

  def run: IO[Unit] = {
    val state     = serviceContext.state
    val neighbors = state.nodeIds.filterNot(_ == state.nodeId).toVector
    fs2.Stream
      .awakeEvery[IO](50.milliseconds)
      .evalMap { _ =>
        neighbors
          .traverse { neighbor =>
            serviceContext.counter.getAndUpdate(_ + 1).flatMap { c =>
              val request = Message(
                messageId = c,
                source = state.nodeId,
                destination = neighbor,
                data = TxnRequestData.Pull
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
  def instance(serviceContext: TxnServiceContext): PullStream =
    new PullStream(serviceContext)
}
