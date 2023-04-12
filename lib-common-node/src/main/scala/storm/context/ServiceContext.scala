package storm.context

import cats.effect.*
import cats.effect.std.Queue

trait ServiceContext {
  def nodeState: Ref[IO, NodeState]
  def messageCounter: Ref[IO, Long]
  def stdoutQueue: Queue[IO, String]
}
