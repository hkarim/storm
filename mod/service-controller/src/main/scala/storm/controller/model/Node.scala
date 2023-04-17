package storm.controller.model

import cats.effect.*
import cats.effect.std.Queue
import fs2.concurrent.SignallingRef
import fs2.io.process.Process
import io.circe.Json

case class Node(
  id: String,
  process: Process[IO],
  signal: SignallingRef[IO, Boolean],
  input: Queue[IO, Json],
  output: Queue[IO, Json],
  error: Queue[IO, String],
)
