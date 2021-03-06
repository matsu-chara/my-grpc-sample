package math.flow

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

import _root_.math.math.MathGrpc.MathStub
import _root_.math.math._
import io.grpc.ClientCall.Listener
import io.grpc.netty.NettyChannelBuilder
import io.grpc.{CallOptions, Metadata, Status}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

object MathFlowControlWorldClient {
  def main(args: Array[String]): Unit = {
    // if use NettyChannelBuilder instead of ChannelBuilder, we can setMaxMessageSize or some options
    val channel = NettyChannelBuilder.forAddress("localhost", 50051).usePlaintext(true).maxMessageSize(1024 * 1024 * 1024).build
    val stub = MathGrpc.stub(channel)
    val client = new MathFlowControlWorldClient(stub)

    val divManyPromise = Promise[Unit]()
    try {
      client.divMany(divManyPromise)
      Await.result(divManyPromise.future, Duration(600, TimeUnit.MINUTES))
    } finally {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
  }
}

class MathFlowControlWorldClient(stub: MathStub) {
  def divMany(promise: Promise[Unit]): Unit = {
    // stub.divMany() Flow-controll version. see http://www.grpc.io/grpc-java/javadoc/
    val call = stub.getChannel.newCall(MathGrpc.METHOD_DIV_MANY, CallOptions.DEFAULT)
    val listener = new Listener[DivReply] {
      private var dividend = 3
      private var divisor = 1

      override def onHeaders(headers: Metadata): Unit = {
        println(s"onHeaders ${headers.toString}")
      }

      override def onReady(): Unit = {
        // ref https://github.com/grpc/grpc-java/blob/0d694c80ee0075b506e545c5ab1d412104eb6e26/core/src/main/java/io/grpc/internal/AbstractStream2.java#L228-L232
        while (call.isReady) {
          if (divisor < 200) {
            val req = DivArgs(dividend, divisor)
            call.sendMessage(req)
            dividend += 2
            divisor += 1
          } else {
            println(s"onReady closing")
            call.halfClose()
          }
        }
      }

      var i = 0

      override def onMessage(message: DivReply): Unit = {
        i = i + 1
        println(s"onMessage + $i")
        Future {
          Thread.sleep(300)
          call.request(1)
        }(concurrent.ExecutionContext.global)
      }

      override def onClose(status: Status, trailers: Metadata): Unit = {
        println(s"onClose status ${status.toString} + trailers: ${trailers.toString}")
        promise.success(())
      }
    }

    call.start(listener, new Metadata)

    // it's needed for get 1st message
    call.request(1)
  }
}
