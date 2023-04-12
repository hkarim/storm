package storm.broadcast.context

import cats.effect.*
import cats.effect.std.Queue
import storm.broadcast.model.BroadcastMessage
import storm.context.ServiceContext

trait LocalServiceContext extends ServiceContext {
  def messages: Ref[IO, Vector[Int]]
  def topology: Ref[IO, Map[String, List[String]]]
  def broadcastQueue: Queue[IO, BroadcastMessage]
  def inFlight: Ref[IO, Map[String, BroadcastMessage]]
}
