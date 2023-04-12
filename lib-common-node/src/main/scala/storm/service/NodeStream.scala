package storm.service

import cats.effect.IO
import io.circe.*
import storm.context.ServiceContext

trait NodeStream[Rq, Rs](val serviceContext: ServiceContext) {

  def onRequest(request: Rq): IO[Option[Rs]]

  def run(using Decoder[Rq], Encoder[Rs]): IO[Unit] =
    fs2.Stream
      .repeatEval(IO.readLine)
      .evalMap(parse)
      .evalMap(onRequest)
      .collect { case Some(v) => v }
      .map(json)
      .evalMap(serviceContext.stdoutQueue.offer)
      .compile
      .drain

  private def parse(line: String)(using Decoder[Rq]): IO[Rq] =
    IO.fromEither {
      io.circe.parser
        .parse(line)
        .flatMap(_.as[Rq])
    }

  private def json(response: Rs)(using Encoder[Rs]): String = {
    import io.circe.syntax.*
    s"${response.asJson.noSpaces}\n"
  }

}
