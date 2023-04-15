package storm.txn.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.txn.model.*

type TxnResponse = Response[TxnResponseBody]

trait TxnResponseBody extends ResponseBody {
  def widen: TxnResponseBody = this
}

object TxnResponseBody {
  given Encoder[TxnResponseBody] =
    Encoder.instance[TxnResponseBody] {
      case v: Txn =>
        Encoder[Txn].apply(v)
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
}
