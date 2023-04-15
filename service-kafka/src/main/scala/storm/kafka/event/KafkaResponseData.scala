package storm.kafka.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.kafka.model.*

sealed trait KafkaResponseData

object KafkaResponseData {

  given DataType[KafkaResponseData] =
    DataType.instance[KafkaResponseData] {
      case _: Send =>
        Send.Type
      case _: Poll =>
        Poll.Type
      case _: CommitOffsets =>
        CommitOffsets.Type
      case _: ListCommittedOffsets =>
        ListCommittedOffsets.Type
    }

  given Encoder[KafkaResponseData] =
    Encoder.instance[KafkaResponseData] {
      case v: Send =>
        Encoder[Send].apply(v)
      case v: Poll =>
        Encoder[Poll].apply(v)
      case v: CommitOffsets =>
        Encoder[CommitOffsets].apply(v)
      case v: ListCommittedOffsets =>
        Encoder[ListCommittedOffsets].apply(v)
    }

  case class Send(inReplyTo: Long, offset: Offset) extends KafkaResponseData

  object Send {
    final val Type: String = "send_ok"
    given Encoder[Send] =
      Encoder.instance[Send] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "offset" -> v.offset.asJson,
        )
      }
  }

  case class Poll(
    inReplyTo: Long,
    messages: Map[Partition, Vector[(Offset, KafkaMessage)]],
  ) extends KafkaResponseData

  object Poll {
    final val Type: String = "poll_ok"
    given Encoder[Poll] =
      Encoder.instance[Poll] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "msgs" -> v.messages.asJson
        )
      }
  }

  case class CommitOffsets(inReplyTo: Long) extends KafkaResponseData

  object CommitOffsets {
    final val Type: String = "commit_offsets_ok"
    given Encoder[CommitOffsets] =
      Encoder.instance[CommitOffsets] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo)
        )
      }
  }

  case class ListCommittedOffsets(inReplyTo: Long, offsets: Map[Partition, Offset]) extends KafkaResponseData

  object ListCommittedOffsets {
    final val Type: String = "list_committed_offsets_ok"
    given Encoder[ListCommittedOffsets] =
      Encoder.instance[ListCommittedOffsets] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "offsets" -> v.offsets.asJson
        )
      }
  }

}
