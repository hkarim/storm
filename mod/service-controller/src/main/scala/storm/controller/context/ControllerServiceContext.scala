package storm.controller.context

import cats.effect.*
import cats.effect.std.Queue
import io.circe.Json
import com.typesafe.config.Config

trait ControllerServiceContext {
  def config: Config
  def counter: Ref[IO, Long]
  def inbound: Queue[IO, Json]
  def outbound: Queue[IO, Json]
}
