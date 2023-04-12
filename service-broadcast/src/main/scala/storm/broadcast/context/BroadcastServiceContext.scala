package storm.broadcast.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import storm.broadcast.model.BroadcastMessage
import storm.context.NodeState
import storm.broadcast.service.{BroadcastNodeStream, PublishStream}
import storm.service.{InitService, StdoutStream}

class BroadcastServiceContext(
  val nodeState: NodeState,
  val messageCounter: Ref[IO, Long],
  val messages: Ref[IO, Vector[Int]],
  val topology: Ref[IO, Map[String, List[String]]],
  val stdoutQueue: Queue[IO, String],
  val messageQueue: Queue[IO, BroadcastMessage]
) extends LocalServiceContext

object BroadcastServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        nodeState      <- InitService.run
        messageCounter <- Ref.of[IO, Long](1L)
        messages       <- Ref.of[IO, Vector[Int]](Vector.empty)
        topology       <- Ref.of[IO, Map[String, List[String]]](Map.empty)
        stdoutQueue    <- Queue.unbounded[IO, String]
        messageQueue   <- Queue.unbounded[IO, BroadcastMessage]
        serviceContext = new BroadcastServiceContext(
          nodeState = nodeState,
          messageCounter = messageCounter,
          messages = messages,
          topology = topology,
          stdoutQueue = stdoutQueue,
          messageQueue = messageQueue,
        )
        _       <- supervisor.supervise(StdoutStream.instance(stdoutQueue).run)
        _       <- supervisor.supervise(PublishStream.instance(serviceContext).run)
        inbound <- BroadcastNodeStream.instance(serviceContext).run
      } yield inbound
    }

}
