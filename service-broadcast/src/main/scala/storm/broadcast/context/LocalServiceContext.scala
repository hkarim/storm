package storm.broadcast.context

import cats.effect.*
import cats.effect.std.Queue
import storm.context.ServiceContext

trait LocalServiceContext extends ServiceContext {
  def messages: Ref[IO, Vector[Int]]
  def topology: Ref[IO, Map[String, List[String]]]
  def messageQueue: Queue[IO, Int]
}
