package storm.controller

import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import storm.controller.context.DefaultControllerServiceContext
import storm.controller.model.OperationMode

object Service extends CommandIOApp(
    name = "service-controller",
    header = "storm node controller",
    version = "0.0.1"
  ) {

  override def main: Opts[IO[ExitCode]] =
    OperationMode.parse.map { command =>
      DefaultControllerServiceContext
        .run(command)
        .as(ExitCode.Success)
    }

}
