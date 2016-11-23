package math.flow

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

import _root_.math.math._
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import io.grpc.stub.{ServerCallStreamObserver, StreamObserver}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object MathFlowControlWorldServer {
  private val port = 50051

  def main(args: Array[String]) {
    val server = new MathFlowControlWorldServer(ExecutionContext.global)
    server.start()
    server.awaitTermination()
  }
}

class MathFlowControlWorldServer(executionContext: ExecutionContext) {
  // if use NettyServerBuilder instead of ServerBuilder, we can setMaxMessageSize or some options
  private var server: Server = NettyServerBuilder
    .forPort(MathFlowControlWorldServer.port)
    .maxMessageSize(1024 * 1024 * 1024)
    .addService(MathGrpc.bindService(new MathImpl, executionContext))
    .build

  private def start() = {
    server.start()
  }

  private def awaitTermination(): Unit = {
    server.awaitTermination()
  }
}

class MathImpl extends MathGrpc.Math {
  // todo: use call.request ? (can't find way to do that)
  override def divMany(responseObserver: StreamObserver[DivReply]): StreamObserver[DivArgs] = new StreamObserver[DivArgs] {
    private val observer = responseObserver.asInstanceOf[ServerCallStreamObserver[DivReply]] // for calling isReady

    private val responses = new mutable.Queue[DivArgs]()
    private var requestCompleted = false
    private var isOnCompletedCalled = false

    // synchronized does not needed(maybe...) it seems that serialized calling is guaranteed.
    // ref: https://github.com/grpc/grpc-java/blob/v1.0.1/stub/src/main/java/io/grpc/stub/CallStreamObserver.java#L65-L66
    private def respondIfReady() = {
      while (observer.isReady && responses.nonEmpty) {
        val request = responses.dequeue()
        val response = DivReply(request.dividend / request.divisor, request.dividend % request.divisor, mega)
        println(s"responed to ${request.dividend} / ${request.divisor} = ${response.quotient} ... ${response.remainder}")
        observer.onNext(response)
      }
      if (responses.isEmpty && requestCompleted && !isOnCompletedCalled) {
        isOnCompletedCalled = true
        observer.onCompleted()
      }
    }

    observer.setOnReadyHandler(new Runnable {
      override def run(): Unit = {
        println(s"onReady")
        respondIfReady()
      }
    })

    // for force setting isReady to false
    private val mega = "a" * 32768 // 32768 is threshold

    override def onNext(value: DivArgs): Unit = {
      println("onNext")
      responses.enqueue(value)
      respondIfReady()
    }

    override def onCompleted(): Unit = {
      println("onCompleted")
      requestCompleted = true
      respondIfReady()
    }

    override def onError(t: Throwable): Unit = {
      t.printStackTrace()
    }
  }


  // omit
  override def div(request: DivArgs): Future[DivReply] = ???

  override def fib(request: FibArgs, responseObserver: StreamObserver[Num]): Unit = ???

  override def sum(responseObserver: StreamObserver[Num]): StreamObserver[Num] = ???
}
