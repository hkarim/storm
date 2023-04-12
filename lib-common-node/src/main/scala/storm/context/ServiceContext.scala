package storm.context

import cats.effect.*
import cats.effect.std.Queue
import io.circe.Json

trait ServiceContext {
  def nodeState: NodeState
  def messageCounter: Ref[IO, Long]
  def inbound: Queue[IO, Json]
  def outbound: Queue[IO, Json]
}
