package storm.broadcast

import cats.effect.*
import storm.broadcast.context.BroadcastServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    BroadcastServiceContext.run
}
