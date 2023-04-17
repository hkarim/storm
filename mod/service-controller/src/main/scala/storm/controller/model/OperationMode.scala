package storm.controller.model

import cats.syntax.all.*
import com.monovore.decline.*

sealed trait OperationMode

object OperationMode {
  case class Network(process: String, nodes: Int) extends OperationMode

  private final def networkOpts: Opts[Network] =
    Opts.subcommand("network", "create a virtual network of identical nodes") {
      val processOpt = Opts.option[String]("process", "node process binary to spawn", "p")
      val nodesOpt   = Opts.option[Int]("nodes", "number of nodes", "n")
      (processOpt, nodesOpt).mapN {
        case (process, nodes) => Network(process, nodes)
      }
    }

  def parse: Opts[OperationMode] =
    networkOpts
}
