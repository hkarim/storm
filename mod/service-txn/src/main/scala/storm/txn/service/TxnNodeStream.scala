package storm.txn.service

import cats.effect.IO
import storm.service.NodeStream
import storm.model.*
import storm.txn.event.*
import storm.txn.context.TxnServiceContext

class TxnNodeStream(serviceContext: TxnServiceContext) extends NodeStream[TxnRequestData, TxnResponseData](serviceContext) {
  def onRequest(request: Message[TxnRequestData]): IO[Option[TxnResponseData]] =
    request.data match {
      case TxnRequestData.Txn(transaction) =>
        serviceContext.store.modify(_.modify(transaction)).map { t =>
          Some(
            TxnResponseData.Txn(
              inReplyTo = request.messageId,
              transaction = t,
            )
          )
        }

      case TxnRequestData.Pull =>
        serviceContext.store.get.map { store =>
          Some(
            TxnResponseData.Pull(
              inReplyTo = request.messageId,
              store = store,
            )
          )
        }

      case TxnSharedData.AckPull(_, store) =>
        serviceContext.store.update(_.merge(store)).map(_ => None)
    }
}

object TxnNodeStream {
  def instance(serviceContext: TxnServiceContext): TxnNodeStream =
    new TxnNodeStream(serviceContext)
}
