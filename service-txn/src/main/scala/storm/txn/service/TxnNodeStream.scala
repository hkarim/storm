package storm.txn.service

import cats.effect.IO
import storm.service.NodeStream
import storm.model.*
import storm.txn.event.*
import storm.txn.context.TxnServiceContext

class TxnNodeStream(serviceContext: TxnServiceContext) extends NodeStream[TxnRequest, TxnResponse](serviceContext) {
  def onRequest(request: TxnRequest): IO[Option[TxnResponse]] =
    request.body match {
      case TxnRequestBody.Txn(messageId, transaction) =>
        for {
          c <- serviceContext.counter.updateAndGet(_ + 1)
          t <- serviceContext.store.modify(_.modify(transaction))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = TxnResponseBody.Txn(
              messageId = c,
              inReplyTo = messageId,
              transaction = t,
            )
          )
        )

      case TxnRequestBody.Pull(messageId) =>
        for {
          c     <- serviceContext.counter.updateAndGet(_ + 1)
          store <- serviceContext.store.get
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = TxnResponseBody.Pull(
              messageId = c,
              inReplyTo = messageId,
              store = store,
            )
          )
        )

      case TxnRequestBody.AckPull(_, store) =>
        serviceContext.store.update(_.merge(store)).map(_ => None)
    }
}

object TxnNodeStream {
  def instance(serviceContext: TxnServiceContext): TxnNodeStream =
    new TxnNodeStream(serviceContext)
}
