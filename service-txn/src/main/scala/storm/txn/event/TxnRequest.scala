package storm.txn.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.txn.model.*

type TxnRequest = Request[TxnRequestBody]

sealed trait TxnRequestBody extends RequestBody {
  def widen: TxnRequestBody = this
}

object TxnRequestBody {

  given Decoder[TxnRequestBody] =
    Decoder[String].at("type").flatMap {
      case "txn" =>
        Decoder[Txn].map(_.widen)
      case "pull" =>
        Decoder[Pull].map(_.widen)
      case "pull_ok" =>
        Decoder[AckPull].map(_.widen)
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

  case class Pull(
    messageId: Long,
  ) extends TxnRequestBody {
    override final val tpe: String = "pull"
  }

  object Pull {

    given Encoder[Pull] =
      Encoder.instance[Pull] { v =>
        Encoders.request(v)
      }

    given Decoder[Pull] =
      for {
        messageId <- Decoders.messageId
      } yield Pull(
        messageId = messageId,
      )
  }

  case class AckPull(
    messageId: Long,
    store: Store,
  ) extends TxnRequestBody {
    override final val tpe: String = "pull_ok"
  }

  object AckPull {

    given Encoder[AckPull] =
      Encoder.instance[AckPull] { v =>
        Encoders.request(v, "store" -> v.store.asJson)
      }

    given Decoder[AckPull] =
      for {
        messageId <- Decoders.messageId
        store     <- Decoder[Store].at("store")
      } yield AckPull(
        messageId = messageId,
        store = store,
      )
  }
}
