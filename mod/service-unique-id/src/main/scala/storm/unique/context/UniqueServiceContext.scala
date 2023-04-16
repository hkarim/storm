package storm.unique.context

import cats.effect.*
import storm.context.ServiceContext

trait UniqueServiceContext extends ServiceContext {
  def unique: Ref[IO, Long]
}
