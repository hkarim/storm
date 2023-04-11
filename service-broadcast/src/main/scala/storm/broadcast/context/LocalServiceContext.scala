package storm.broadcast.context

import cats.effect.*
import storm.context.ServiceContext

trait LocalServiceContext extends ServiceContext {
  def messages: Ref[IO, List[Long]]
  def topology: Ref[IO, Map[String, List[String]]] 
}
