package storm.echo

import cats.effect.*
import storm.echo.context.DefaultServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    DefaultServiceContext.run
}