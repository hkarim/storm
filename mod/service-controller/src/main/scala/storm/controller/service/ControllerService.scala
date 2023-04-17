package storm.controller.service

import cats.effect.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.{Node, OperationMode}

class ControllerService(serviceContext: ControllerServiceContext) {

  def run(command: OperationMode): Resource[IO, List[Node]] = command match {
    case network: OperationMode.Network =>
      NetworkService
        .instance(serviceContext)
        .resource(network)
  }

}

object ControllerService {
  def instance(serviceContext: ControllerServiceContext): ControllerService =
    new ControllerService(serviceContext)
}
