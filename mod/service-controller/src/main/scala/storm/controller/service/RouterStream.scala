package storm.controller.service

import cats.effect.*
import io.circe.*
import fs2.*
import storm.controller.model.Node

object RouterStream {

  def run(nodes: List[Node], node: Node): IO[Unit] = {
    val net: Map[String, Node] =
      nodes
        .filterNot(_.id == node.id)
        .foldLeft(Map.empty[String, Node]) { (acc, node) =>
          acc.updated(node.id, node)
        }

    Stream
      .fromQueueUnterminated[IO, Json](node.output)
      .map { json =>
        val destinationNode =
          json.hcursor.downField("dest").as[String] match {
            case Left(_)  => Option.empty[Node]
            case Right(v) => net.get(v)
          }
        (json, destinationNode)
      }
      .collect { case (json, Some(n)) => (json, n) }
      .evalMap {
        case (json, n) => n.input.offer(json)
      }
      .compile
      .drain
  }

}
