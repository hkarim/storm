package storm.kafka.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.Json
import storm.context.NodeState
import storm.kafka.model.*
import storm.kafka.service.KafkaNodeStream
import storm.service.{InitService, StdinStream, StdoutStream}

class DefaultKafkaServiceContext(
  val config: Config,
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val replica: Ref[IO, Replica],
  val commits: Ref[IO, Commits],
) extends KafkaServiceContext

object DefaultKafkaServiceContext {
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
        replica  <- Ref.of[IO, Replica](Replica.empty)
        commits  <- Ref.of[IO, Commits](Commits.empty)
        serviceContext = new DefaultKafkaServiceContext(
          config = config,
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
          replica = replica,
          commits = commits,
        )
        stream <- KafkaNodeStream.instance(serviceContext).run
      } yield stream
    }

}
