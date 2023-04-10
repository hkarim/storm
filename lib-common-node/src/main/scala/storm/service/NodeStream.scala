package storm.service

import cats.effect.IO
import storm.context.ServiceContext
import storm.event.{Request, Response}

trait NodeStream(val serviceContext: ServiceContext) {

  def onRequest(request: Request): IO[Response]

  def run: IO[Unit] =
    fs2.io.stdin[IO](1024)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .mapAsync(32) { parse }
      .mapAsync(32) { onRequest }
      .mapAsync(32) { json }
      .through(fs2.text.utf8.encode)
      .through(fs2.io.stdout)
      .compile
      .drain

  private def parse(line: String): IO[Request] =
    IO.fromEither {
      io.circe.parser
        .parse(line)
        .flatMap(_.as[Request])
    }


  private def json(response: Response): IO[String] = {
    import io.circe.syntax.*
    IO.pure(s"${response.asJson.noSpaces}\n")
  }

}
