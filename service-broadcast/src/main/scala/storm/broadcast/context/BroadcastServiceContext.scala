package storm.broadcast.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import storm.context.NodeState
import storm.broadcast.service.{BroadcastNodeStream, PublishStream}

class BroadcastServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Long],
  val messages: Ref[IO, List[Int]],
  val topology: Ref[IO, Map[String, List[String]]],
  val queue: Queue[IO, Int]
) extends LocalServiceContext

object BroadcastServiceContext {
  def run: IO[Unit] = {
    Supervisor[IO].use { supervisor =>
      for {
        nodeState <- Ref.of[IO, NodeState](NodeState.Uninitialized)
        messageCounter <- Ref.of[IO, Long](1L)
        messages <- Ref.of[IO, List[Int]](Nil)
        topology <- Ref.of[IO, Map[String, List[String]]](Map.empty)
        queue <- Queue.unbounded[IO, Int]
        serviceContext = new BroadcastServiceContext(
          nodeState = nodeState,
          messageCounter = messageCounter,
          messages = messages,
          topology = topology,
          queue = queue,
        )
        _ <- supervisor.supervise(PublishStream.instance(serviceContext).run)
        inbound <- BroadcastNodeStream.instance(serviceContext).run
      } yield inbound
    }

  }
}
