package storm.txn.event

import io.circe.*
import storm.model.*
import storm.txn.model.*

type TxnRequest = Request[TxnRequestBody]

trait TxnRequestBody extends RequestBody {
  def widen: TxnRequestBody = this
}

object TxnRequestBody {

  given Decoder[TxnRequestBody] =
    Decoder[String].at("type").flatMap {
      case "txn" =>
        Decoder[Txn].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized txn request type `$otherwise`", Nil))
    }

  case class Txn(
    messageId: Long,
    transaction: Vector[Record],
  ) extends TxnRequestBody {
    override final val tpe: String = "txn"
  }

  given Decoder[Txn] =
    for {
      messageId   <- Decoders.messageId
      transaction <- Decoder[Vector[Record]].at("txn")
    } yield Txn(
      messageId = messageId,
      transaction = transaction,
    )
}
