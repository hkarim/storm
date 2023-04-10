package storm.echo.service

import cats.effect.*
import storm.echo.context.ServiceContext
import storm.event.{Request, Response}

class NodeStream(val serviceContext: ServiceContext) {

  private final val initializationService: InitializationService =
    InitializationService.instance(serviceContext)
  private final val echoService: EchoService =
    EchoService.instance(serviceContext)

  private def parse(line: String): IO[Request] =
    IO.fromEither {
      io.circe.parser
        .parse(line)
        .flatMap(_.as[Request])
    }

  private def onRequest(request: Request): IO[Response] = request match {
    case request: Request.InitializationRequest =>
      initializationService.onRequest(request)
    case request: Request.EchoRequest =>
      echoService.onRequest(request)
  }

  private def json(response: Response): IO[String] = {
    import io.circe.syntax.*
    IO.pure(s"${response.asJson.noSpaces}\n")
  }

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

}

object NodeStream {
  def instance(serviceContext: ServiceContext): NodeStream =
    new NodeStream(serviceContext)
}
