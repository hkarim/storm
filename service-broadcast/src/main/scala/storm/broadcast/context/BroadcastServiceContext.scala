package storm.broadcast.context

import cats.effect.*
import storm.context.NodeState
import storm.broadcast.service.BroadcastNodeStream

class BroadcastServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Long],
  val messages: Ref[IO, List[Long]],
  val topology: Ref[IO, Map[String, List[String]]],
) extends LocalServiceContext

object BroadcastServiceContext {
  def run: IO[Unit] =
    for {
      nodeState      <- Ref.of[IO, NodeState](NodeState.Uninitialized)
      messageCounter <- Ref.of[IO, Long](1L)
      messages       <- Ref.of[IO, List[Long]](Nil)
      topology       <- Ref.of[IO, Map[String, List[String]]](Map.empty)
      serviceContext = new BroadcastServiceContext(
        nodeState = nodeState,
        messageCounter = messageCounter,
        messages = messages,
        topology = topology,
      )
      stream <- BroadcastNodeStream.instance(serviceContext).run
    } yield stream
}
