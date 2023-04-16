package storm.controller.node

import cats.effect.*
import cats.effect.std.Queue
import io.circe.{Json, parser}
import fs2.*
import fs2.io.process.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.OperationMode

class NodeProcess(serviceContext: ControllerServiceContext) {

  val counter: Ref[IO, Long] = serviceContext.counter

  private def inputStream(node: Node): IO[Unit] =
    Stream
      .fromQueueUnterminated(node.input)
      .map { json =>
        s"${json.noSpaces}\n"
      }
      .through(fs2.text.utf8.encode[IO])
      .through(node.process.stdin)
      .compile
      .drain

  private def outputStream(node: Node): IO[Unit] =
    node.process.stdout
      .through(fs2.text.utf8.decode[IO])
      .through(fs2.text.lines[IO])
      .evalMap { line =>
        IO.fromEither {
          parser.parse(line)
        }
      }
      .evalMap(node.output.tryOffer)
      .compile
      .drain

  def resource(id: String, network: OperationMode.Network): Resource[IO, Node] =
    for {
      process <- ProcessBuilder(network.process).spawn[IO]
      input   <- Resource.eval(Queue.unbounded[IO, Json])
      output  <- Resource.eval(Queue.unbounded[IO, Json])
      node = Node(id, process, input, output)
      _ <- inputStream(node).background
      _ <- outputStream(node).background
    } yield node
}

object NodeProcess {
  def instance(serviceContext: ControllerServiceContext): NodeProcess =
    new NodeProcess(serviceContext)
}
