package math

import _root_.math.math._
import io.grpc.stub.StreamObserver
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

object MathWorldServer {
  private val port = 50051

  def main(args: Array[String]) {
    val server = new MathWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class MathWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(MathWorldServer.port).addService(MathGrpc.bindService(new MathImpl, executionContext)).build.start

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        self.stop()
      }
    })
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class MathImpl extends MathGrpc.Math {
    override def div(request: DivArgs): Future[DivReply] = {
      Future.successful(DivReply(request.dividend / request.divisor, request.dividend % request.divisor))
    }

    override def divMany(responseObserver: StreamObserver[DivReply]): StreamObserver[DivArgs] = new DivStreamObserver(responseObserver)

    override def fib(request: FibArgs, responseObserver: StreamObserver[Num]): Unit = {
      var x_n0 = 0
      var x_n1 = 1
      while (x_n0 < request.limit) {
        responseObserver.onNext(Num(x_n0))
        var x_n2 = x_n0 + x_n1
        x_n0 = x_n1
        x_n1 = x_n2
      }
      responseObserver.onCompleted()
    }

    override def sum(responseObserver: StreamObserver[Num]): StreamObserver[Num] = new SumStreamObserver(responseObserver)

    class DivStreamObserver(responseObserver: StreamObserver[DivReply]) extends StreamObserver[DivArgs] {
      override def onError(t: Throwable): Unit = t.printStackTrace()
      override def onCompleted(): Unit = {
        responseObserver.onCompleted()
        println("divMany finished")
      }
      override def onNext(value: DivArgs): Unit = {
        println(value)
        responseObserver.onNext(DivReply(value.dividend / value.divisor, value.dividend % value.divisor))
      }
    }

    class SumStreamObserver(responseObserver: StreamObserver[Num]) extends StreamObserver[Num] {
      var sum = 0L

      override def onError(t: Throwable): Unit = t.printStackTrace()
      override def onCompleted(): Unit = {
        println("sum finished")
        responseObserver.onNext(Num(sum))
        responseObserver.onCompleted()
      }
      override def onNext(value: Num): Unit = {
        println(value)
        sum = sum + value.num
      }
    }
  }
}

