package storm.controller.context

import cats.effect.*
import com.typesafe.config.Config

trait ControllerServiceContext {
  def config: Config
  def counter: Ref[IO, Long]
}
