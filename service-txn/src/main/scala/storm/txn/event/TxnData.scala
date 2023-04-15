package storm.txn.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.txn.model.*

sealed trait TxnRequestData {
  def widen: TxnRequestData = this
}

object TxnRequestData {

  given DataType[TxnRequestData] =
    DataType.instance[TxnRequestData] {
      case _: Txn =>
        Txn.Type
      case Pull =>
        Pull.Type
      case _: TxnSharedData.AckPull =>
        TxnSharedData.AckPull.Type
    }

  given Decoder[TxnRequestData] =
    Decoder[String].at("type").flatMap {
      case Txn.Type =>
        Decoder[Txn].map(_.widen)
      case Pull.Type =>
        Decoder[Pull.type].map(_.widen)
      case TxnSharedData.AckPull.Type =>
        Decoder[TxnSharedData.AckPull].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized txn request type `$otherwise`", Nil))
    }

  case class Txn(transaction: Vector[Record]) extends TxnRequestData

  object Txn {
    final val Type: String = "txn"
    given Decoder[Txn] =
      for {
        transaction <- Decoder[Vector[Record]].at("txn")
      } yield Txn(
        transaction = transaction,
      )
  }

  case object Pull extends TxnRequestData {
    final val Type: String = "pull"

    given Encoder[Pull.type] =
      Encoder.instance[Pull.type] { _ =>
        Json.obj()
      }

    given Decoder[Pull.type] =
      Decoder.const(Pull)
  }

}

sealed trait TxnResponseData

object TxnResponseData {
  given DataType[TxnResponseData] =
    DataType.instance[TxnResponseData] {
      case _: Txn =>
        Txn.Type
      case _: Pull =>
        Pull.Type
      case _: TxnSharedData.AckPull =>
        TxnSharedData.AckPull.Type
    }

  given Encoder[TxnResponseData] =
    Encoder.instance[TxnResponseData] {
      case v: Txn =>
        Encoder[Txn].apply(v)
      case v: Pull =>
        Encoder[Pull].apply(v)
      case v: TxnSharedData.AckPull =>
        Encoder[TxnSharedData.AckPull].apply(v)
    }

  case class Txn(inReplyTo: Long, transaction: Vector[Record]) extends TxnResponseData

  object Txn {
    final val Type: String = "txn_ok"
    given Encoder[Txn] =
      Encoder.instance[Txn] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "txn" -> v.transaction.asJson
        )
      }
  }

  case class Pull(inReplyTo: Long, store: Store) extends TxnResponseData

  object Pull {
    final val Type: String = "pull_ok"

    given Encoder[Pull] =
      Encoder.instance[Pull] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "store" -> v.store.asJson,
        )
      }

    given Decoder[Pull] =
      for {
        inReplyTo <- Message.Decoders.inReplyTo
        store     <- Decoder[Store].at("store")
      } yield Pull(
        inReplyTo = inReplyTo,
        store = store,
      )
  }
}

sealed trait TxnSharedData
  extends TxnRequestData with TxnResponseData

object TxnSharedData {
  case class AckPull(inReplyTo: Long, store: Store) extends TxnSharedData

  object AckPull {
    final val Type: String = "pull_ok"

    given Encoder[AckPull] =
      Encoder.instance[AckPull] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "store" -> v.store.asJson,
        )
      }

    given Decoder[AckPull] =
      for {
        inReplyTo <- Message.Decoders.inReplyTo
        store     <- Decoder[Store].at("store")
      } yield AckPull(
        inReplyTo = inReplyTo,
        store = store,
      )
  }
}
