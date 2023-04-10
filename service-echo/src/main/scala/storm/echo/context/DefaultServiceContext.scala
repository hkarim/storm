package storm.echo.context

import cats.effect.*
import storm.echo.node.NodeState
import storm.echo.service.NodeStream

class DefaultServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Long],
) extends ServiceContext


object DefaultServiceContext {
  def run: IO[Unit] =
    for {
      nodeState <- Ref.of[IO, NodeState](NodeState.Uninitialized)
      messageCounter <- Ref.of[IO, Long](1L)
      serviceContext = new DefaultServiceContext(
        nodeState = nodeState,
        messageCounter = messageCounter
      )
      stream <- NodeStream.instance(serviceContext).run
    } yield stream
}
