package ch.becompany.akka.io

import akka.stream.scaladsl.FlowOps

trait Processor[Out, Mat] {

  def apply[T <: FlowOps[Out, Mat]](flow: T): T

}
