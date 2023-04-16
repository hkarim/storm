package storm.controller.context

import cats.effect.std.{Queue, Supervisor}
import cats.effect.{IO, Ref}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import storm.controller.model.OperationMode
import storm.controller.service.ControllerService
import storm.service.{StdinStream, StdoutStream}

class DefaultControllerServiceContext(
  val config: Config,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
) extends ControllerServiceContext

object DefaultControllerServiceContext {
  def run(command: OperationMode): IO[Unit] =
    Supervisor[IO].use { supervisor =>
      val config: Config = ConfigFactory.load()
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        counter  <- Ref.of[IO, Long](1L)
        serviceContext = new DefaultControllerServiceContext(
          config = config,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
        )
        controller <- ControllerService.instance(serviceContext).run(command)
      } yield controller
    }
}
