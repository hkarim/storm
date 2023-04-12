package storm.echo.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import storm.context.{NodeState, ServiceContext}
import storm.echo.service.EchoNodeStream
import storm.service.{InitService, StdinStream, StdoutStream}

class EchoServiceContext(
  val nodeState: NodeState,
  val messageCounter: Ref[IO, Long],
  val inbound: Queue[IO, String],
  val outbound: Queue[IO, String],
) extends ServiceContext

object EchoServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound        <- Queue.unbounded[IO, String]
        outbound       <- Queue.unbounded[IO, String]
        _              <- supervisor.supervise(StdinStream.instance(inbound).run)
        _              <- supervisor.supervise(StdoutStream.instance(outbound).run)
        nodeState      <- InitService.instance(inbound, outbound).run
        messageCounter <- Ref.of[IO, Long](1L)
        serviceContext = new EchoServiceContext(
          nodeState = nodeState,
          messageCounter = messageCounter,
          inbound = inbound,
          outbound = outbound,
        )
        stream <- EchoNodeStream.instance(serviceContext).run
      } yield stream
    }

}
