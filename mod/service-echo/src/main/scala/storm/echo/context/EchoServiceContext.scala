package storm.echo.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import storm.context.{NodeState, ServiceContext}
import storm.echo.service.EchoNodeStream
import storm.service.{InitService, StdinStream, StdoutStream}

class EchoServiceContext(
  val config: Config,
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
) extends ServiceContext

object EchoServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      val config: Config = ConfigFactory.load()
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state    <- InitService.instance(inbound, outbound).run
        counter  <- Ref.of[IO, Long](1L)
        serviceContext = new EchoServiceContext(
          config = config,
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
        )
        stream <- EchoNodeStream.instance(serviceContext).run
      } yield stream
    }

}
