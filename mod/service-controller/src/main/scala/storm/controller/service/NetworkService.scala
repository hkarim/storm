package storm.controller.service

import cats.effect.*
import cats.syntax.all.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.OperationMode
import storm.controller.node.{Node, NodeProcess}

class NetworkService(serviceContext: ControllerServiceContext) {

  def resource(network: OperationMode.Network): Resource[IO, List[Node]] =
    (1 to network.nodes).toList.map(n => s"n$n").traverse { id =>
      NodeProcess
        .instance(serviceContext)
        .resource(id, network)
    }

}

object NetworkService {
  def instance(serviceContext: ControllerServiceContext): NetworkService =
    new NetworkService(serviceContext)
}
