package storm.kafka.event

import io.circe.*
import storm.model.*
import storm.kafka.model.*

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
    partition: Partition,
    message: Message,
  ) extends KafkaRequestBody {
    override final val tpe: String = "send"
  }

  given Decoder[Send] =
    for {
      messageId <- Decoders.messageId
      key       <- Decoder[Partition].at("key")
      message   <- Decoder[Message].at("msg")
    } yield Send(
      messageId = messageId,
      partition = key,
      message = message,
    )

  case class Poll(
    messageId: Long,
    offsets: Map[Partition, Offset],
  ) extends KafkaRequestBody {
    override final val tpe: String = "poll"
  }

  given Decoder[Poll] =
    for {
      messageId <- Decoders.messageId
      offsets   <- Decoder[Map[Partition, Offset]].at("offsets")
    } yield Poll(
      messageId = messageId,
      offsets = offsets,
    )

  case class CommitOffsets(
    messageId: Long,
    offsets: Map[Partition, Offset],
  ) extends KafkaRequestBody {
    override final val tpe: String = "commit_offsets"
  }

  given Decoder[CommitOffsets] =
    for {
      messageId <- Decoders.messageId
      offsets   <- Decoder[Map[Partition, Offset]].at("offsets")
    } yield CommitOffsets(
      messageId = messageId,
      offsets = offsets,
    )

  case class ListCommittedOffsets(
    messageId: Long,
    partitions: Vector[Partition],
  ) extends KafkaRequestBody {
    override final val tpe: String = "list_committed_offsets"
  }

  given Decoder[ListCommittedOffsets] =
    for {
      messageId  <- Decoders.messageId
      partitions <- Decoder[Vector[Partition]].at("keys")
    } yield ListCommittedOffsets(
      messageId = messageId,
      partitions = partitions,
    )

}
