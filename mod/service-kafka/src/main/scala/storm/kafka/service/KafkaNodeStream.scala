package storm.kafka.service

import cats.effect.*
import storm.model.*
import storm.kafka.context.KafkaServiceContext
import storm.kafka.event.*
import storm.service.NodeStream

class KafkaNodeStream(serviceContext: KafkaServiceContext) extends NodeStream[KafkaRequestData, KafkaResponseData](serviceContext) {

  def onRequest(request: Message[KafkaRequestData]): IO[Option[KafkaResponseData]] =
    request.data match {
      case KafkaRequestData.Send(partition, message) =>
        serviceContext.replica.modify(_.put(partition, message)).map { offset =>
          Some(
            KafkaResponseData.Send(
              inReplyTo = request.messageId,
              offset = offset,
            )
          )
        }

      case KafkaRequestData.Poll(offsets) =>
        serviceContext.replica.modify(x => (x, x.poll(offsets))).map { messages =>
          Some(
            KafkaResponseData.Poll(
              inReplyTo = request.messageId,
              messages = messages,
            )
          )
        }

      case KafkaRequestData.CommitOffsets(offsets) =>
        serviceContext.commits.update(_.commit(offsets)).map { _ =>
          Some(
            KafkaResponseData.CommitOffsets(
              inReplyTo = request.messageId,
            )
          )
        }

      case KafkaRequestData.ListCommittedOffsets(keys) =>
        serviceContext.commits.modify(x => (x, x.list(keys))).map { offsets =>
          Some(
            KafkaResponseData.ListCommittedOffsets(
              inReplyTo = request.messageId,
              offsets = offsets,
            )
          )
        }
    }

}

object KafkaNodeStream {
  def instance(serviceContext: KafkaServiceContext): KafkaNodeStream =
    new KafkaNodeStream(serviceContext)
}
