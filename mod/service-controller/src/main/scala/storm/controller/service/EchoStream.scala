package storm.controller.service

import cats.effect.*

import scala.concurrent.duration.*
import storm.controller.model.{Message, Node}

object EchoStream {

  def run(controllerId: String, counter: Ref[IO, Long], node: Node): IO[Unit] =
    fs2.Stream
      .awakeEvery[IO](2.seconds)
      .evalMap { duration =>
        counter.getAndUpdate(_ + 1).map { messageId =>
          Message.echo(
            messageId = messageId,
            source = controllerId,
            destination = node.id,
            echo = s"$duration"
          )
        }
      }
      .evalMap(node.input.offer)
      .compile
      .drain

}
