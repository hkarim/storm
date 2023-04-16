package storm.controller.node

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
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

  def run(id: String, network: OperationMode.Network): IO[Node] =
    ProcessBuilder(network.process).spawn[IO].use { process =>
      Supervisor[IO](await = true).use { supervisor =>
        for {
          input  <- Queue.unbounded[IO, Json]
          output <- Queue.unbounded[IO, Json]
          node = Node(id, process, input, output)
          _ <- supervisor.supervise(inputStream(node))
          _ <- supervisor.supervise(outputStream(node))
        } yield node
      }

    }
}

object NodeProcess {
  def instance(serviceContext: ControllerServiceContext): NodeProcess =
    new NodeProcess(serviceContext)
}
