package storm.controller.service

import cats.effect.*
import cats.syntax.all.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.*

class NetworkService(serviceContext: ControllerServiceContext) {

  private final val controllerPrefix: String =
    serviceContext.config.getString("service.controller-prefix")
  private final val nodePrefix: String =
    serviceContext.config.getString("service.node-prefix")

  private def sendInit(id: String, node: Node, nodes: List[Node]): IO[Unit] =
    serviceContext
      .counter
      .getAndUpdate(_ + 1).flatMap { messageId =>
        node.input.offer(
          Message.init(messageId, id, node.id, node.id, nodes.map(_.id))
        )
      }

  def resource(network: OperationMode.Network): Resource[IO, List[Node]] =
    for {
      // get an id for this network controller
      id <- Resource.eval(
        serviceContext
          .counter
          .getAndUpdate(_ + 1)
          .map(c => s"$controllerPrefix$c")
      )
      // spawn the nodes
      nodes <- (1 to network.nodes).toList
        .map(n => s"$nodePrefix$n")
        .traverse { nodeId =>
          NodeProcess
            .instance(serviceContext)
            .resource(nodeId, network)
        }
      // send an init message to each node
      // this has to be done in the foreground
      // so nodes can initialize properly
      send = nodes.traverse(node => sendInit(id, node, nodes))
      _ <- Resource.eval(send)
      // spawn a handler stream for each node
      _ <- nodes.traverse { node =>
        RouterStream
          .run(nodes, node)
          .background
      }
    } yield nodes

}

object NetworkService {
  def instance(serviceContext: ControllerServiceContext): NetworkService =
    new NetworkService(serviceContext)
}
