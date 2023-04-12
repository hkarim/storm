package storm.service

import cats.effect.*
import cats.effect.std.Queue

class StdoutStream(queue: Queue[IO, String]) {
  
  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(queue)
      .through(fs2.text.utf8.encode[IO])
      .through(fs2.io.stdout[IO])
      .compile
      .drain

}

object StdoutStream {
  def instance(queue: Queue[IO, String]): StdoutStream =
    new StdoutStream(queue)
}
