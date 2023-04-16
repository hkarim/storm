package storm.kafka

import cats.effect.*
import storm.kafka.context.DefaultKafkaServiceContext

object Service extends IOApp.Simple {
  override def run: IO[Unit] =
    DefaultKafkaServiceContext.run
}
