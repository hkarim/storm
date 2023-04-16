package storm.service

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import storm.context.ServiceContext
import storm.model.{DataType, Message}

trait NodeStream[Rq, Rs](val serviceContext: ServiceContext) {

  def onRequest(request: Message[Rq]): IO[Option[Rs]]

  def run(using Decoder[Rq], Encoder[Rs], DataType[Rs]): IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(serviceContext.inbound)
      .parEvalMapUnorderedUnbounded { json =>
        IO.fromEither {
          json.as[Message[Rq]]
        }
      }
      .parEvalMapUnorderedUnbounded { rq =>
        onRequest(rq).map { rs =>
          (rq, rs)
        }
      }
      .collect { case (rq, Some(rs)) => (rq, rs) }
      .evalMap {
        case (rq, rs) =>
          serviceContext.counter.getAndUpdate(_ + 1).map { c =>
            Message.response(
              request = rq,
              id = c,
              data = rs,
            ).asJson
          }
      }
      .parEvalMapUnorderedUnbounded(serviceContext.outbound.tryOffer)
      .compile
      .drain

}
