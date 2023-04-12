package storm.service

import cats.effect.*
import cats.effect.std.Queue
import io.circe.*

class StdinStream(inbound: Queue[IO, Json]) {
  def run: IO[Unit] =
    fs2.io.stdin[IO](1024)
      .through(fs2.text.utf8.decode[IO])
      .through(fs2.text.lines[IO])
      .evalMap { line =>
        IO.fromEither {
          parser.parse(line)
        }
      }
      .evalMap(inbound.offer)
      .compile
      .drain
}

object StdinStream {
  def instance(inbound: Queue[IO, Json]): StdinStream =
    new StdinStream(inbound)
}
