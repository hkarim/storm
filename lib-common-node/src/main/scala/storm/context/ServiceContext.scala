package storm.context

import cats.effect.*
import cats.effect.std.Queue
import com.typesafe.config.Config
import io.circe.Json

trait ServiceContext {
  def config: Config
  def state: NodeState
  def counter: Ref[IO, Long]
  def inbound: Queue[IO, Json]
  def outbound: Queue[IO, Json]
}
