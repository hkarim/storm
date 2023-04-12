package storm.broadcast.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import io.circe.Json
import storm.broadcast.model.BroadcastMessage
import storm.context.NodeState
import storm.broadcast.service.{BroadcastNodeStream, PublishStream}
import storm.service.{InitService, StdinStream, StdoutStream}

class BroadcastServiceContext(
  val nodeState: NodeState,
  val messageCounter: Ref[IO, Long],
  val messages: Ref[IO, Vector[Int]],
  val topology: Ref[IO, Map[String, List[String]]],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val messageQueue: Queue[IO, BroadcastMessage]
) extends LocalServiceContext

object BroadcastServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound        <- Queue.unbounded[IO, Json]
        outbound       <- Queue.unbounded[IO, Json]
        _              <- supervisor.supervise(StdinStream.instance(inbound).run)
        _              <- supervisor.supervise(StdoutStream.instance(outbound).run)
        nodeState      <- InitService.instance(inbound, outbound).run
        messageCounter <- Ref.of[IO, Long](1L)
        messages       <- Ref.of[IO, Vector[Int]](Vector.empty)
        topology       <- Ref.of[IO, Map[String, List[String]]](Map.empty)
        messageQueue   <- Queue.unbounded[IO, BroadcastMessage]
        serviceContext = new BroadcastServiceContext(
          nodeState = nodeState,
          messageCounter = messageCounter,
          messages = messages,
          topology = topology,
          inbound = inbound,
          outbound = outbound,
          messageQueue = messageQueue,
        )
        _              <- supervisor.supervise(PublishStream.instance(serviceContext).run)
        broadcast <- BroadcastNodeStream.instance(serviceContext).run
      } yield broadcast
    }

}
