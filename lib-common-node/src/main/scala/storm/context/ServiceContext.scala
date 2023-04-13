package storm.context

import cats.effect.*
import cats.effect.std.Queue
import io.circe.Json

trait ServiceContext {
  def state: NodeState
  def counter: Ref[IO, Long]
  def inbound: Queue[IO, Json]
  def outbound: Queue[IO, Json]
}
