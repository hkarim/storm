package storm.service

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import storm.context.ServiceContext

trait NodeStream[Rq, Rs](val serviceContext: ServiceContext) {

  def onRequest(request: Rq): IO[Option[Rs]]

  def run(using Decoder[Rq], Encoder[Rs]): IO[Unit] =
    fs2.Stream
      .fromQueueUnterminated(serviceContext.inbound)
      .parEvalMapUnorderedUnbounded { json =>
        IO.fromEither {
          json.as[Rq]
        }
      }
      .parEvalMapUnorderedUnbounded(onRequest)
      .collect { case Some(v) => v }
      .map(_.asJson)
      .parEvalMapUnorderedUnbounded(serviceContext.outbound.tryOffer)
      .compile
      .drain

}
