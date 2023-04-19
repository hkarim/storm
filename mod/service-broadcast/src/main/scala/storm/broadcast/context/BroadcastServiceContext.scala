package storm.broadcast.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import storm.broadcast.service.*
import storm.context.{NodeState, ServiceContext}
import storm.service.{InitService, StdinStream, StdoutStream}

class BroadcastServiceContext(
  val config: Config,
  val state: NodeState,
  val counter: Ref[IO, Long],
  val messages: Ref[IO, Vector[Int]],
  val topology: Ref[IO, Map[String, List[String]]],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
) extends ServiceContext

object BroadcastServiceContext {
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
        messages <- Ref.of[IO, Vector[Int]](Vector.empty)
        topology <- Ref.of[IO, Map[String, List[String]]](Map.empty)
        serviceContext = new BroadcastServiceContext(
          config = config,
          state = state,
          counter = counter,
          messages = messages,
          topology = topology,
          inbound = inbound,
          outbound = outbound,
        )
        _         <- supervisor.supervise(ReadStream.instance(serviceContext).run)
        broadcast <- BroadcastNodeStream.instance(serviceContext).run
      } yield broadcast
    }

}
