package storm.service

import cats.effect.*
import cats.effect.std.Queue

class StdinStream(inbound: Queue[IO, String]) {
  def run: IO[Unit] =
    fs2.io.stdin[IO](1024)
      .through(fs2.text.utf8.decode[IO])
      .through(fs2.text.lines[IO])
      .evalMap(inbound.offer)
      .compile
      .drain
}

object StdinStream {
  def instance(inbound: Queue[IO, String]): StdinStream =
    new StdinStream(inbound)
}
