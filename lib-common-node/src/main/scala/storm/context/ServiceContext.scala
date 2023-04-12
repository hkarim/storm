package storm.context

import cats.effect.*
import cats.effect.std.Queue

trait ServiceContext {
  def nodeState: NodeState
  def messageCounter: Ref[IO, Long]
  def inbound: Queue[IO, String]
  def outbound: Queue[IO, String]
}
