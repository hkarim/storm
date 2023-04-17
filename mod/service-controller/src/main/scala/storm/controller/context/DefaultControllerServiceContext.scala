package storm.controller.context

import cats.effect.{IO, Ref}
import com.typesafe.config.{Config, ConfigFactory}
import storm.controller.model.OperationMode
import storm.controller.service.ControllerService

class DefaultControllerServiceContext(
  val config: Config,
  val counter: Ref[IO, Long],
) extends ControllerServiceContext

object DefaultControllerServiceContext {
  def run(command: OperationMode): IO[Unit] = {
    val config: Config = ConfigFactory.load()
    for {
      counter <- Ref.of[IO, Long](1L)
      serviceContext = new DefaultControllerServiceContext(
        config = config,
        counter = counter,
      )
      controller <- ControllerService
        .instance(serviceContext)
        .run(command)
        .useForever
    } yield controller
  }
}
