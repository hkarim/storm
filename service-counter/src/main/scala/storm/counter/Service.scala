package storm.counter

import cats.effect.*
import storm.counter.context.CounterServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    CounterServiceContext.run
}