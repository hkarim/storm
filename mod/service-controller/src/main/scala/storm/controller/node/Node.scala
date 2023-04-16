package storm.controller.node

import cats.effect.*
import cats.effect.std.Queue
import fs2.io.process.Process
import io.circe.Json

case class Node(
  id: String,
  process: Process[IO],
  input: Queue[IO, Json],
  output: Queue[IO, Json],
)
