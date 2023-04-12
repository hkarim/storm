package storm.service

import cats.effect.*
import cats.effect.std.Queue

class StdoutStream(queue: Queue[IO, String]) {
  
  def run: IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(queue)
      .evalMap(IO.print)
      .compile
      .drain

}

object StdoutStream {
  def instance(queue: Queue[IO, String]): StdoutStream =
    new StdoutStream(queue)
}
