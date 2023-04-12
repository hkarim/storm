package storm.context

import cats.effect.*
import cats.effect.std.Queue

trait ServiceContext {
  def nodeState: NodeState
  def messageCounter: Ref[IO, Long]
  def stdoutQueue: Queue[IO, String]
}
