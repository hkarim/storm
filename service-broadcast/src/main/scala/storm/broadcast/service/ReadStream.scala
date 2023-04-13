package storm.broadcast.service

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import scala.concurrent.duration.*
import storm.broadcast.context.LocalServiceContext
import storm.broadcast.event.BroadcastRequestBody
import storm.model.Request

class ReadStream(serviceContext: LocalServiceContext) {

//  def run: IO[Unit] =
//    fs2.Stream
//      .awakeEvery[IO](150.milliseconds)
//      .evalMap(_ => serviceContext.topology.get)
//      .map(_.get(serviceContext.state.nodeId))
//      .collect { case Some(v) => v }
//      .evalMap { neighbors =>
//        neighbors
//          .traverse { neighbor =>
//            serviceContext.counter.getAndUpdate(_ + 1).flatMap { c =>
//              val request = Request(
//                source = serviceContext.state.nodeId,
//                destination = neighbor,
//                body = BroadcastRequestBody.Read(
//                  messageId = c,
//                )
//              )
//              serviceContext.outbound.offer(request.asJson)
//            }
//          }
//      }
//      .compile
//      .drain

  def run: IO[Unit] = {
    val state      = serviceContext.state
    val candidates = state.nodeIds.filterNot(_ == state.nodeId).toVector
    val groups     = candidates.grouped(5).toVector
    fs2.Stream
      .awakeEvery[IO](50.milliseconds)
      .map(_ => scala.util.Random.between(0, 10000))
      .evalMap { random =>
        val neighbors = groups(random % groups.length)
        neighbors
          .traverse { neighbor =>
            serviceContext.counter.getAndUpdate(_ + 1).flatMap { c =>
              val request = Request(
                source = serviceContext.state.nodeId,
                destination = neighbor,
                body = BroadcastRequestBody.Read(
                  messageId = c,
                )
              )
              serviceContext.outbound.offer(request.asJson)
            }
          }
      }
      .compile
      .drain
  }
}

object ReadStream {
  def instance(serviceContext: LocalServiceContext): ReadStream =
    new ReadStream(serviceContext)
}
