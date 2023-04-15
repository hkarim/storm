package storm.unique.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import io.circe.Json
import storm.context.{NodeState, ServiceContext}
import storm.unique.service.UniqueNodeStream
import storm.service.{InitService, StdinStream, StdoutStream}

class DefaultUniqueServiceContext(
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val unique: Ref[IO, Long],
) extends UniqueServiceContext

object DefaultUniqueServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state    <- InitService.instance(inbound, outbound).run
        counter  <- Ref.of[IO, Long](1L)
        unique   <- Ref.of[IO, Long](1L)
        serviceContext = new DefaultUniqueServiceContext(
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
          unique = unique,
        )
        stream <- UniqueNodeStream.instance(serviceContext).run
      } yield stream
    }

}
