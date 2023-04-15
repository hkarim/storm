package storm.txn.context

import cats.effect.*
import cats.effect.std.*
import io.circe.Json
import storm.context.NodeState
import storm.service.*
import storm.txn.model.Store
import storm.txn.service.{PullStream, TxnNodeStream}

class DefaultTxnServiceContext(
  val state: NodeState,
  val counter: Ref[IO, Long],
  val inbound: Queue[IO, Json],
  val outbound: Queue[IO, Json],
  val store: Ref[IO, Store],
) extends TxnServiceContext

object DefaultTxnServiceContext {
  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      for {
        inbound  <- Queue.unbounded[IO, Json]
        outbound <- Queue.unbounded[IO, Json]
        _        <- supervisor.supervise(StdinStream.instance(inbound).run)
        _        <- supervisor.supervise(StdoutStream.instance(outbound).run)
        state    <- InitService.instance(inbound, outbound).run
        counter  <- Ref.of[IO, Long](1L)
        store    <- Ref.of[IO, Store](Store.empty)
        serviceContext = new DefaultTxnServiceContext(
          state = state,
          counter = counter,
          inbound = inbound,
          outbound = outbound,
          store = store,
        )
        _      <- supervisor.supervise(PullStream.instance(serviceContext).run)
        stream <- TxnNodeStream.instance(serviceContext).run
      } yield stream
    }
}
