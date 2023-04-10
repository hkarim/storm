package storm.echo.context

import cats.effect.*
import storm.echo.node.NodeState

trait ServiceContext {
  def nodeState: Ref[IO, NodeState]
  def messageCounter: Ref[IO, Long]
}
