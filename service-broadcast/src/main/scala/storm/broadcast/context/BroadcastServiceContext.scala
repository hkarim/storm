package storm.broadcast.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import io.circe.Json
import storm.broadcast.model.BroadcastMessage
import storm.context.NodeState
import storm.broadcast.service.{BroadcastNodeStream, BroadcastStream, FaultToleranceStream}
import storm.service.{InitService, StdinStream, StdoutStream}

class BroadcastServiceContext(
  val state: NodeState,
  val counter: Ref[IO, Long],
  val messages: Ref[IO, Vector[Int]],
  val topology: Ref[IO, Map[String, List[String]]],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val broadcastQueue: Queue[IO, BroadcastMessage],
  val inFlight: Ref[IO, Map[String, BroadcastMessage]],
) extends LocalServiceContext

object BroadcastServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound        <- Queue.unbounded[IO, Json]
        outbound       <- Queue.unbounded[IO, Json]
        _              <- supervisor.supervise(StdinStream.instance(inbound).run)
        _              <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state          <- InitService.instance(inbound, outbound).run
        counter        <- Ref.of[IO, Long](1L)
        messages       <- Ref.of[IO, Vector[Int]](Vector.empty)
        topology       <- Ref.of[IO, Map[String, List[String]]](Map.empty)
        broadcastQueue <- Queue.unbounded[IO, BroadcastMessage]
        inFlightQueue  <- Ref.of[IO, Map[String, BroadcastMessage]](Map.empty)
        serviceContext = new BroadcastServiceContext(
          state = state,
          counter = counter,
          messages = messages,
          topology = topology,
          inbound = inbound,
          outbound = outbound,
          broadcastQueue = broadcastQueue,
          inFlight = inFlightQueue,
        )
        _         <- supervisor.supervise(BroadcastStream.instance(serviceContext).run)
        _         <- supervisor.supervise(FaultToleranceStream.instance(serviceContext).run)
        broadcast <- BroadcastNodeStream.instance(serviceContext).run
      } yield broadcast
    }

}
