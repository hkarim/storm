package storm.controller.service

import cats.effect.*
import cats.effect.std.*
import io.circe.{Json, parser}
import fs2.*
import fs2.concurrent.SignallingRef
import fs2.io.process.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.{Node, OperationMode}

class NodeProcess(serviceContext: ControllerServiceContext) {

  val counter: Ref[IO, Long] = serviceContext.counter

  private def inputStream(node: Node): IO[Unit] =
    Stream
      .fromQueueUnterminated(node.input)
      .interruptWhen(node.signal)
      .map(_.noSpaces)
      .evalTap { line =>
        Console[IO].println(s"[${node.id}::in] $line")
      }
      .map(line => s"$line\n")
      .through(fs2.text.utf8.encode[IO])
      .through(node.process.stdin)
      .compile
      .drain

  private def outputStream(node: Node): IO[Unit] =
    node.process.stdout
      .interruptWhen(node.signal)
      .through(fs2.text.utf8.decode[IO])
      .through(fs2.text.lines[IO])
      .evalTap { line =>
        Console[IO].println(s"[${node.id}::out] $line")
      }
      .evalMap { line =>
        IO.fromEither {
          parser.parse(line)
        }
      }
      .evalMap(node.output.offer)
      .compile
      .drain

  private def errorStream(node: Node): IO[Unit] =
    node.process.stderr
      .interruptWhen(node.signal)
      .through(fs2.text.utf8.decode[IO])
      .through(fs2.text.lines[IO])
      .evalTap { line =>
        Console[IO].println(s"[${node.id}::error] $line")
      }
      .evalMap(node.error.offer)
      .compile
      .drain

  def resource(id: String, network: OperationMode.Network): Resource[IO, Node] =
    for {
      process <- ProcessBuilder(network.process).spawn[IO]
      input   <- Resource.eval(Queue.unbounded[IO, Json])
      output  <- Resource.eval(Queue.unbounded[IO, Json])
      error   <- Resource.eval(Queue.unbounded[IO, String])
      signal  <- Resource.eval(SignallingRef.of[IO, Boolean](false))
      node = Node(
        id = id,
        process = process,
        signal = signal,
        input = input,
        output = output,
        error = error
      )
      _ <- inputStream(node).background
      _ <- outputStream(node).background
      _ <- errorStream(node).background
    } yield node
}

object NodeProcess {
  def instance(serviceContext: ControllerServiceContext): NodeProcess =
    new NodeProcess(serviceContext)
}
