package storm.kafka.event

import io.circe.*
import storm.model.*
import storm.kafka.model.*

sealed trait KafkaRequestData {
  def widen: KafkaRequestData = this
}

object KafkaRequestData {

  given DataType[KafkaRequestData] =
    DataType.instance[KafkaRequestData] {
      case _: Send =>
        Send.Type
      case _: Poll =>
        Poll.Type
      case _: CommitOffsets =>
        CommitOffsets.Type
      case _: ListCommittedOffsets =>
        ListCommittedOffsets.Type
    }

  given Decoder[KafkaRequestData] =
    Decoder[String].at("type").flatMap {
      case Send.Type =>
        Decoder[Send].map(_.widen)
      case Poll.Type =>
        Decoder[Poll].map(_.widen)
      case CommitOffsets.Type =>
        Decoder[CommitOffsets].map(_.widen)
      case ListCommittedOffsets.Type =>
        Decoder[ListCommittedOffsets].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized kafka request type `$otherwise`", Nil))
    }

  case class Send(partition: Partition, message: KafkaMessage) extends KafkaRequestData

  object Send {
    final val Type: String = "send"
    given Decoder[Send] =
      for {
        key     <- Decoder[Partition].at("key")
        message <- Decoder[KafkaMessage].at("msg")
      } yield Send(
        partition = key,
        message = message,
      )
  }

  case class Poll(offsets: Map[Partition, Offset]) extends KafkaRequestData

  object Poll {
    final val Type: String = "poll"
    given Decoder[Poll] =
      for {
        offsets <- Decoder[Map[Partition, Offset]].at("offsets")
      } yield Poll(
        offsets = offsets,
      )
  }

  case class CommitOffsets(offsets: Map[Partition, Offset]) extends KafkaRequestData

  object CommitOffsets {
    final val Type: String = "commit_offsets"
    given Decoder[CommitOffsets] =
      for {
        offsets <- Decoder[Map[Partition, Offset]].at("offsets")
      } yield CommitOffsets(
        offsets = offsets,
      )
  }

  case class ListCommittedOffsets(partitions: Vector[Partition]) extends KafkaRequestData

  object ListCommittedOffsets {
    final val Type: String = "list_committed_offsets"

    given Decoder[ListCommittedOffsets] =
      for {
        partitions <- Decoder[Vector[Partition]].at("keys")
      } yield ListCommittedOffsets(
        partitions = partitions,
      )
  }

}
