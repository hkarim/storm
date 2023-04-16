package storm.counter.context

import cats.effect.{IO, Ref}
import cats.effect.std.Queue
import io.circe.Json
import storm.context.{NodeState, ServiceContext}

trait CounterServiceContext extends ServiceContext {
  def state: NodeState
  def counter: Ref[IO, Long]
  def inbound: Queue[IO, Json]
  def outbound: Queue[IO, Json]
  def delta: Ref[IO, Map[String, Int]]
}
