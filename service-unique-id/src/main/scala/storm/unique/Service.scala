package storm.unique

import cats.effect.*
import storm.unique.context.UniqueServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    UniqueServiceContext.run
}