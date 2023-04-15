package storm.txn.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.txn.model.*

type TxnResponse = Response[TxnResponseBody]

sealed trait TxnResponseBody extends ResponseBody {
  def widen: TxnResponseBody = this
}

object TxnResponseBody {
  given Encoder[TxnResponseBody] =
    Encoder.instance[TxnResponseBody] {
      case v: Txn =>
        Encoder[Txn].apply(v)
      case v: Pull =>
        Encoder[Pull].apply(v)
    }

  case class Txn(
    messageId: Long,
    inReplyTo: Long,
    transaction: Vector[Record],
  ) extends TxnResponseBody {
    override final val tpe: String = "txn_ok"
  }

  object Txn {
    given Encoder[Txn] =
      Encoder.instance[Txn] { v =>
        Encoders.response(v, "txn" -> v.transaction.asJson)
      }
  }

  case class Pull(
    messageId: Long,
    inReplyTo: Long,
    store: Store,
  ) extends TxnResponseBody {
    override final val tpe: String = "pull_ok"
  }

  object Pull {

    given Encoder[Pull] =
      Encoder.instance[Pull] { v =>
        Encoders.response(v, "store" -> v.store.asJson)
      }

    given Decoder[Pull] =
      for {
        messageId <- Decoders.messageId
        inReplyTo <- Decoders.inReplyTo
        store     <- Decoder[Store].at("store")
      } yield Pull(
        messageId = messageId,
        inReplyTo = inReplyTo,
        store = store,
      )
  }
}
