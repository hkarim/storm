package storm.service

import cats.effect.*
import cats.effect.std.Queue

class StdoutStream(outbound: Queue[IO, String]) {
  
  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(outbound)
      .through(fs2.text.utf8.encode[IO])
      .through(fs2.io.stdout[IO])
      .compile
      .drain

}

object StdoutStream {
  def instance(outbound: Queue[IO, String]): StdoutStream =
    new StdoutStream(outbound)
}
