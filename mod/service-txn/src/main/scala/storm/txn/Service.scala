package storm.txn

import cats.effect.*
import storm.txn.context.DefaultTxnServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    DefaultTxnServiceContext.run
}
