package storm.txn.service

import cats.effect.IO
import storm.service.NodeStream
import storm.txn.event.*
import storm.txn.context.TxnServiceContext

class TxnNodeStream(serviceContext: TxnServiceContext) extends NodeStream[TxnRequest, TxnResponse](serviceContext) {
  def onRequest(request: TxnRequest): IO[Option[TxnResponse]] = ???
}

object TxnNodeStream {
  def instance(serviceContext: TxnServiceContext): TxnNodeStream =
    new TxnNodeStream(serviceContext)
}
