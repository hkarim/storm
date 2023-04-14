package storm.kafka.event

import io.circe.*
import storm.model.*

type KafkaRequest = Request[KafkaRequestBody]

sealed trait KafkaRequestBody extends RequestBody {
  def widen: KafkaRequestBody = this
}

object KafkaRequestBody {

  given Decoder[KafkaRequestBody] =
    Decoder[String].at("type").flatMap {
      case "send" =>
        Decoder[Send].map(_.widen)
      case "poll" =>
        Decoder[Poll].map(_.widen)
      case "commit_offsets" =>
        Decoder[CommitOffsets].map(_.widen)
      case "list_committed_offsets" =>
        Decoder[ListCommittedOffsets].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized kafka request type `$otherwise`", Nil))
    }

  case class Send(
    messageId: Long,
    key: String,
    message: Option[Int],
  ) extends KafkaRequestBody {
    override final val tpe: String = "send"
  }

  given Decoder[Send] =
    for {
      messageId <- Decoders.messageId
      key       <- Decoder[String].at("key")
      message   <- Decoder[Option[Int]].at("msg")
    } yield Send(
      messageId = messageId,
      key = key,
      message = message,
    )

  case class Poll(
    messageId: Long,
    offsets: Map[String, Int],
  ) extends KafkaRequestBody {
    override final val tpe: String = "poll"
  }

  given Decoder[Poll] =
    for {
      messageId <- Decoders.messageId
      offsets   <- Decoder[Map[String, Int]].at("offsets")
    } yield Poll(
      messageId = messageId,
      offsets = offsets,
    )

  case class CommitOffsets(
    messageId: Long,
    offsets: Map[String, Int],
  ) extends KafkaRequestBody {
    override final val tpe: String = "commit_offsets"
  }

  given Decoder[CommitOffsets] =
    for {
      messageId <- Decoders.messageId
      offsets   <- Decoder[Map[String, Int]].at("offsets")
    } yield CommitOffsets(
      messageId = messageId,
      offsets = offsets,
    )

  case class ListCommittedOffsets(
    messageId: Long,
    keys: Vector[String],
  ) extends KafkaRequestBody {
    override final val tpe: String = "list_committed_offsets"
  }

  given Decoder[ListCommittedOffsets] =
    for {
      messageId <- Decoders.messageId
      keys      <- Decoder[Vector[String]].at("keys")
    } yield ListCommittedOffsets(
      messageId = messageId,
      keys = keys,
    )

}
