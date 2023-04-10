package storm.echo.context

import cats.effect.*
import storm.echo.node.NodeState

class DefaultServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Int],
) extends ServiceContext


object DefaultServiceContext {
  def run: IO[Unit] = ???
}
