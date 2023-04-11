package storm.echo.context

import cats.effect.*
import storm.context.{NodeState, ServiceContext}
import storm.echo.service.EchoNodeStream

class EchoServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Long],
) extends ServiceContext

object EchoServiceContext {
  def run: IO[Unit] =
    for {
      nodeState      <- Ref.of[IO, NodeState](NodeState.Uninitialized)
      messageCounter <- Ref.of[IO, Long](1L)
      serviceContext = new EchoServiceContext(
        nodeState = nodeState,
        messageCounter = messageCounter
      )
      stream <- EchoNodeStream.instance(serviceContext).run
    } yield stream
}
