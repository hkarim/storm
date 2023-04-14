package storm.counter

import cats.effect.*
import storm.counter.context.DefaultCounterServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    DefaultCounterServiceContext.run
}
