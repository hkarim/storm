package storm.echo

import cats.effect.*

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    IO.println("storm")
}