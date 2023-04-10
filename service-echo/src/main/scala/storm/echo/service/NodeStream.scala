package storm.echo.service

import cats.effect.*
import storm.echo.context.ServiceContext
import storm.event.Request

class NodeStream(val serviceContext: ServiceContext) {

  private final val initializationService: InitializationService =
    InitializationService.instance(serviceContext)
  private final val echoService: EchoService =
    EchoService.instance(serviceContext)

  def run: IO[Unit] =
    fs2.io.stdin[IO](1024)
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .evalMap { line =>
        for {
          json <- IO.fromEither {
            io.circe.parser.parse(line)
          }
          request <- IO.fromEither {
            json.as[Request]
          }
        } yield request
      }
      .evalMap {
        case request: Request.InitializationRequest =>
          initializationService.onRequest(request)
        case request: Request.EchoRequest =>
          echoService.onRequest(request)
      }
      .evalMap { response =>
        import io.circe.syntax.*
        IO.pure(s"${response.asJson.noSpaces}\n")
      }
      .through(fs2.text.utf8.encode)
      .through(fs2.io.stdout)
      .compile
      .drain

}

object NodeStream {
  def instance(serviceContext: ServiceContext): NodeStream =
    new NodeStream(serviceContext)
}
