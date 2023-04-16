package storm.controller.service

import cats.syntax.all.*
import cats.effect.*
import cats.effect.std.Supervisor
import storm.controller.context.ControllerServiceContext
import storm.controller.model.OperationMode
import storm.controller.node.NodeProcess

class NetworkService(serviceContext: ControllerServiceContext) {

  def run(network: OperationMode.Network): IO[Unit] =
    Supervisor[IO].use { supervisor =>
      val nodeIds = (1 to network.nodes).toList.map(n => s"n$n")
      nodeIds.traverse { id =>
        supervisor.supervise {
          NodeProcess
            .instance(serviceContext)
            .run(id, network)
        }
      }.void
    }

}

object NetworkService {
  def instance(serviceContext: ControllerServiceContext): NetworkService =
    new NetworkService(serviceContext)
}
