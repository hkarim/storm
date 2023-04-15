package storm.txn.context

import cats.effect.*
import storm.context.ServiceContext
import storm.txn.model.*

trait TxnServiceContext extends ServiceContext {
  def store: Ref[IO, Store]
}
