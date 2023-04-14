package storm.kafka.service

import cats.effect.*
import storm.model.*
import storm.kafka.context.KafkaServiceContext
import storm.kafka.event.*
import storm.service.NodeStream

class KafkaNodeStream(serviceContext: KafkaServiceContext) extends NodeStream[KafkaRequest, KafkaResponse](serviceContext) {

  def onRequest(request: KafkaRequest): IO[Option[KafkaResponse]] =
    request.body match {
      case KafkaRequestBody.Send(messageId, key, message) =>
        for {
          c      <- serviceContext.counter.getAndUpdate(_ + 1)
          offset <- serviceContext.replica.modify(_.put(key, message))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = KafkaResponseBody.Send(
              messageId = c,
              inReplyTo = messageId,
              offset = offset,
            )
          )
        )

      case KafkaRequestBody.Poll(messageId, offsets) =>
        for {
          c        <- serviceContext.counter.getAndUpdate(_ + 1)
          messages <- serviceContext.replica.modify(x => (x, x.poll(offsets)))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = KafkaResponseBody.Poll(
              messageId = c,
              inReplyTo = messageId,
              messages = messages,
            )
          )
        )

      case KafkaRequestBody.CommitOffsets(messageId, offsets) =>
        for {
          c <- serviceContext.counter.getAndUpdate(_ + 1)
          _ <- serviceContext.commits.update(_.commit(offsets))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = KafkaResponseBody.CommitOffsets(
              messageId = c,
              inReplyTo = messageId,
            )
          )
        )

      case KafkaRequestBody.ListCommittedOffsets(messageId, keys) =>
        for {
          c       <- serviceContext.counter.getAndUpdate(_ + 1)
          offsets <- serviceContext.commits.modify(x => (x, x.list(keys)))
        } yield Some(
          Response(
            source = serviceContext.state.nodeId,
            destination = request.source,
            body = KafkaResponseBody.ListCommittedOffsets(
              messageId = c,
              inReplyTo = messageId,
              offsets = offsets,
            )
          )
        )
    }

}

object KafkaNodeStream {
  def instance(serviceContext: KafkaServiceContext): KafkaNodeStream =
    new KafkaNodeStream(serviceContext)
}
