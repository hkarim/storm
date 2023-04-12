package storm.service

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import storm.context.{NodeState, ServiceContext}
import storm.model.*

trait NodeStream[Rq, Rs](val serviceContext: ServiceContext) {

  def onRequest(request: Rq): IO[Option[Rs]]

  private def onInit(json: Json): IO[Json] =
    for {
      request      <- IO.fromEither(json.as[Request[InitializationRequestBody]])
      currentState <- serviceContext.nodeState.get
      r <- currentState match {
        case NodeState.Uninitialized =>
          val s = NodeState.Initialized(request.body.nodeId, request.body.nodeIds)
          for {
            _ <- serviceContext.nodeState.set(s)
            c <- serviceContext.messageCounter.getAndUpdate(_ + 1)
            response = Response[InitializationResponseBody](
              source = request.body.nodeId,
              destination = request.source,
              body = InitializationResponseBody(
                messageId = Some(c),
                inReplyTo = request.body.messageId,
              )
            )
          } yield response.asJson
        case otherwise =>
          IO.raiseError(new IllegalStateException(s"invalid state: ${otherwise.description}"))
      }
    } yield r

  def run(using Decoder[Rq], Encoder[Rs]): IO[Unit] =
    fs2.io.stdin[IO](256)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .take(1)
      .evalMap { line =>
        IO.fromEither(io.circe.parser.parse(line))
      }
      .evalMap(onInit)
      .map { json => s"${json.noSpaces}\n" }
      //.through(fs2.text.utf8.encode)
      //.through(fs2.io.stdout)
      .evalMap(serviceContext.stdoutQueue.offer)
      .compile
      .drain
      .flatMap { _ =>
        mainStream
      }

  private def mainStream(using Decoder[Rq], Encoder[Rs]): IO[Unit] =
    fs2.io.stdin[IO](2048)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .mapAsync(64) {
        parse
      }
      .mapAsync(64) {
        onRequest
      }
      .collect { case Some(v) => v }
      .mapAsync(64) {
        json
      }
      .through(fs2.text.utf8.encode)
      .through(fs2.io.stdout)
      .compile
      .drain

  private def parse(line: String)(using Decoder[Rq]): IO[Rq] =
    IO.fromEither {
      io.circe.parser
        .parse(line)
        .flatMap(_.as[Rq])
    }

  private def json(response: Rs)(using Encoder[Rs]): IO[String] = {
    import io.circe.syntax.*
    IO.pure(s"${response.asJson.noSpaces}\n")
  }

}
