package storm.counter.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import io.circe.Json
import storm.context.NodeState
import storm.counter.service.{CounterNodeStream, PullStream}
import storm.service.{InitService, StdinStream, StdoutStream}

class DefaultCounterServiceContext(
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val delta: Ref[IO, Map[String, Int]],
) extends CounterServiceContext

object DefaultCounterServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state    <- InitService.instance(inbound, outbound).run
        counter  <- Ref.of[IO, Long](1L)
        delta    <- Ref.of[IO, Map[String, Int]](Map.empty)
        serviceContext = new DefaultCounterServiceContext(
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
          delta = delta,
        )
        _      <- supervisor.supervise(PullStream.instance(serviceContext).run)
        stream <- CounterNodeStream.instance(serviceContext).run
      } yield stream
    }

}
