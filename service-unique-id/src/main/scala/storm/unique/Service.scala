package storm.unique

import cats.effect.*
import storm.unique.context.DefaultUniqueServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    DefaultUniqueServiceContext.run
}
