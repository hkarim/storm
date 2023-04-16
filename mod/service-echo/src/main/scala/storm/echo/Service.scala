package storm.echo

import cats.effect.*
import storm.echo.context.EchoServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    EchoServiceContext.run
}
