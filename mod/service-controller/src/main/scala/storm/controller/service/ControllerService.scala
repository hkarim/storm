package storm.controller.service

import cats.effect.*
import storm.controller.context.ControllerServiceContext
import storm.controller.model.OperationMode

class ControllerService(serviceContext: ControllerServiceContext) {

  def run(command: OperationMode): IO[Unit] = command match {
    case network: OperationMode.Network =>
      NetworkService
        .instance(serviceContext)
        .resource(network)
        .useForever
  }

}

object ControllerService {
  def instance(serviceContext: ControllerServiceContext): ControllerService =
    new ControllerService(serviceContext)
}
