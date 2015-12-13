package math

import java.util.concurrent.TimeUnit

import _root_.math.math.MathGrpc.MathStub
import _root_.math.math._
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ManagedChannelBuilder}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object MathWorldClient {
  def apply(host: String, port: Int): MathWorldClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
    val stub = MathGrpc.stub(channel)
    new MathWorldClient(channel, stub)
  }

  def main(args: Array[String]): Unit = {
    val client = MathWorldClient("localhost", 50051)
    try {
      client.div()
      client.divMany()
      client.fib()
      client.sum()
    } finally {
      client.shutdown()
    }
  }
}

class MathWorldClient private(
  private val channel: ManagedChannel,
  private val stub: MathStub
) {
  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def div(): Unit = {
    try {
      val request = DivArgs(1304, 34)
      println(request)
      val response = Await.result(stub.div(request), Duration.Inf)
      println(response)
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

  def divMany(): Unit = {
    val request: StreamObserver[DivArgs] = stub.divMany(new DivResponseStream())

    (1 to 11).zip(3 to 13).foreach { case (dividend, divisor) =>
      request.onNext(DivArgs(dividend, divisor))
    }
    request.onCompleted()
  }

  def fib(): Unit = {
    stub.fib(FibArgs(200), new FibResponseStream)
  }

  def sum(): Unit = {
    val request: StreamObserver[Num] = stub.sum(new SumResponseStream)
    (1 to 100).foreach(i => request.onNext(Num(i)))
    request.onCompleted()
  }

  private class DivResponseStream extends StreamObserver[DivReply] {
    override def onError(t: Throwable): Unit = t.printStackTrace()
    override def onCompleted(): Unit = println("divMany finished")
    override def onNext(value: DivReply): Unit = println(value)
  }

  private class FibResponseStream extends StreamObserver[Num] {
    override def onError(t: Throwable): Unit = t.printStackTrace()
    override def onCompleted(): Unit = println("fib finished")
    override def onNext(value: Num): Unit = println(value)
  }

  private class SumResponseStream extends StreamObserver[Num] {
    override def onError(t: Throwable): Unit = t.printStackTrace()
    override def onCompleted(): Unit = println("sum finished")
    override def onNext(value: Num): Unit = println(value)
  }
}
