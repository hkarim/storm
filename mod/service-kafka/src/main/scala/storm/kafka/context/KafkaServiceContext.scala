package storm.kafka.context

import cats.effect.*
import storm.context.ServiceContext
import storm.kafka.model.*

trait KafkaServiceContext extends ServiceContext {
  def replica: Ref[IO, Replica]
  def commits: Ref[IO, Commits]
}
