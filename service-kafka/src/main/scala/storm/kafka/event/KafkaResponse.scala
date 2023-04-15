package storm.kafka.event

import io.circe.*
import io.circe.syntax.*
import storm.model.*
import storm.kafka.model.*

type KafkaResponse = Response[KafkaResponseBody]

sealed trait KafkaResponseBody extends ResponseBody

object KafkaResponseBody {

  given Encoder[KafkaResponseBody] =
    Encoder.instance[KafkaResponseBody] {
      case v: Send =>
        Encoder[Send].apply(v)
      case v: Poll =>
        Encoder[Poll].apply(v)
      case v: CommitOffsets =>
        Encoder[CommitOffsets].apply(v)
      case v: ListCommittedOffsets =>
        Encoder[ListCommittedOffsets].apply(v)
    }

  case class Send(
    messageId: Long,
    inReplyTo: Long,
    offset: Offset,
  ) extends KafkaResponseBody {
    override final val tpe: String = "send_ok"
  }

  object Send {
    given Encoder[Send] =
      Encoder.instance[Send] { v =>
        Encoders.response(v, "offset" -> v.offset.asJson)
      }
  }

  case class Poll(
    messageId: Long,
    inReplyTo: Long,
    messages: Map[Partition, Vector[(Offset, Message)]],
  ) extends KafkaResponseBody {
    override final val tpe: String = "poll_ok"
  }

  object Poll {
    given Encoder[Poll] =
      Encoder.instance[Poll] { v =>
        Encoders.response(v, "msgs" -> v.messages.asJson)
      }
  }

  case class CommitOffsets(
    messageId: Long,
    inReplyTo: Long,
  ) extends KafkaResponseBody {
    override final val tpe: String = "commit_offsets_ok"
  }

  object CommitOffsets {
    given Encoder[CommitOffsets] =
      Encoder.instance[CommitOffsets] { v =>
        Encoders.response(v)
      }
  }

  case class ListCommittedOffsets(
    messageId: Long,
    inReplyTo: Long,
    offsets: Map[Partition, Offset],
  ) extends KafkaResponseBody {
    override final val tpe: String = "list_committed_offsets_ok"
  }

  object ListCommittedOffsets {
    given Encoder[ListCommittedOffsets] =
      Encoder.instance[ListCommittedOffsets] { v =>
        Encoders.response(v, "offsets" -> v.offsets.asJson)
      }
  }

}
