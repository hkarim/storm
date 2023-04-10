package storm.context

import cats.effect.*

trait ServiceContext {
  def nodeState: Ref[IO, NodeState]
  def messageCounter: Ref[IO, Long]
}
