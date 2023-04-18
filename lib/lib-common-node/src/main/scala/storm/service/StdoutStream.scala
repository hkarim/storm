package storm.service

import cats.effect.*
import cats.effect.std.*
import io.circe.Json

class StdoutStream(outbound: Queue[IO, Json]) {

  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(outbound)
      .map(json => s"${json.noSpaces}\n")
      .through(fs2.text.utf8.encode[IO])
      .through(fs2.io.stdout[IO])
      .compile
      .drain

}

object StdoutStream {
  def instance(outbound: Queue[IO, Json]): StdoutStream =
    new StdoutStream(outbound)
}
