package storm.unique.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import io.circe.Json
import storm.context.{NodeState, ServiceContext}
import storm.unique.service.UniqueNodeStream
import storm.service.{InitService, StdinStream, StdoutStream}

class UniqueServiceContext(
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
) extends ServiceContext

object UniqueServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state    <- InitService.instance(inbound, outbound).run
        counter  <- Ref.of[IO, Long](1L)
        serviceContext = new UniqueServiceContext(
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
        )
        stream <- UniqueNodeStream.instance(serviceContext).run
      } yield stream
    }

}
