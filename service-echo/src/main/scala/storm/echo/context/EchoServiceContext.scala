package storm.echo.context

import cats.effect.*
import cats.effect.std.{Queue, Supervisor}
import storm.context.{NodeState, ServiceContext}
import storm.echo.service.EchoNodeStream
import storm.service.StdoutStream

class EchoServiceContext(
  val nodeState: Ref[IO, NodeState],
  val messageCounter: Ref[IO, Long],
  val stdoutQueue: Queue[IO, String],
) extends ServiceContext

object EchoServiceContext {
  def run: IO[Unit] = {
    Supervisor[IO].use { supervisor =>
      for {
        nodeState <- Ref.of[IO, NodeState](NodeState.Uninitialized)
        messageCounter <- Ref.of[IO, Long](1L)
        stdoutQueue <- Queue.unbounded[IO, String]
        serviceContext = new EchoServiceContext(
          nodeState = nodeState,
          messageCounter = messageCounter,
          stdoutQueue = stdoutQueue,
        )
        _ <- supervisor.supervise(StdoutStream.instance(stdoutQueue).run)
        stream <- EchoNodeStream.instance(serviceContext).run
      } yield stream 
    }
    
  }
}
